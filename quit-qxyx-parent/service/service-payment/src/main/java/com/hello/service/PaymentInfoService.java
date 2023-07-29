package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.enums.PaymentType;
import com.hello.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo>{
    /**
     * 查询订单支付状态
     * @param orderNo
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(String orderNo, PaymentType paymentType);

    /**
     * 添加订单支付状态
     * @param orderNo
     * @return
     */
    PaymentInfo savePaymentInfo(String orderNo);

    /**
     * 订单支付成功,更改之前的订单支付状态,并进行库存更新
     * @param outTradeNo
     * @param paymentType
     * @param resultMap
     */
    void paySuccess(String outTradeNo, PaymentType paymentType, Map<String, String> resultMap);
}
