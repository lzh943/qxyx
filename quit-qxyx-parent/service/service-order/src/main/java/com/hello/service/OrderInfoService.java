package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.order.OrderInfo;
import com.hello.result.Result;
import com.hello.vo.order.OrderSubmitVo;
import com.hello.vo.order.OrderUserQueryVo;

public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 确认订单
     * @return
     */
    Result confirmOrder(Long userId);

    /**
     * 生成订单
     * @param orderSubmitVo
     * @return
     */
    Result submitOrder(OrderSubmitVo orderSubmitVo,Long userId);

    /**
     * 获取订单详情
     * @param orderId
     * @return
     */
    Result getOrderInfoById(Long orderId);

    /**
     * 获取订单详情(远程调用使用)
     * @param orderNo
     * @return
     */
    OrderInfo getOderInfoByOrderNo(String orderNo);

    /**
     * 订单已支付,更新库存
     * @param orderNo
     */
    void orderPay(String orderNo);

    /**
     * 获取用户订单分页列表
     * @param page
     * @param limit
     * @param orderUserQueryVo
     * @return
     */
    Result findUserOrderPage(Long page, Long limit, OrderUserQueryVo orderUserQueryVo);
}
