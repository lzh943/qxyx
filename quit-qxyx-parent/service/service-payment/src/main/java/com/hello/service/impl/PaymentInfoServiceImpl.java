package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.constant.MqConst;
import com.hello.enums.PaymentStatus;
import com.hello.enums.PaymentType;
import com.hello.exception.SysException;
import com.hello.mapper.PaymentInfoMapper;
import com.hello.model.order.OrderInfo;
import com.hello.model.order.PaymentInfo;
import com.hello.order.OrderFeignClient;
import com.hello.result.ResultCodeEnum;
import com.hello.service.PaymentInfoService;
import com.hello.service.RabbitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private RabbitService rabbitService;
    /**
     * 查询订单支付状态
     */
    @Override
    public PaymentInfo getPaymentInfo(String orderNo, PaymentType paymentType) {
        LambdaQueryWrapper<PaymentInfo> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderNo, orderNo);
        PaymentInfo paymentInfo = getOne(wrapper);
        return paymentInfo;
    }

    /**
     * 添加订单支付状态
     */
    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        OrderInfo orderInfo = orderFeignClient.getOderInfoByOrderNo(orderNo);
        if(orderInfo==null){
            throw new SysException(ResultCodeEnum.DATA_ERROR);
        }
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(PaymentType.WEIXIN);
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setOrderNo(orderInfo.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "userId:"+orderInfo.getUserId()+"下订单";
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));//为了测试，统一支付0.01元
        save(paymentInfo);
        return paymentInfo;
    }

    /**
     * 订单支付成功,更改之前的订单支付状态,并进行库存更新
     */
    @Override
    public void paySuccess(String orderNo, PaymentType paymentType, Map<String, String> resultMap) {
        PaymentInfo paymentInfo = getOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderNo, orderNo));
        if(paymentInfo.getPaymentStatus()!=PaymentStatus.UNPAID){
            return;
        }
        paymentInfo.setPaymentStatus(PaymentStatus.PAID);
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackContent(resultMap.toString());
        updateById(paymentInfo);
        rabbitService.sendMessage(MqConst.EXCHANGE_PAY_DIRECT,MqConst.ROUTING_PAY_SUCCESS,orderNo);
    }
}
