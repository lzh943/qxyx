package com.hello.controller;

import com.hello.auth.AuthContextHolder;
import com.hello.model.order.CartInfo;
import com.hello.model.order.OrderInfo;
import com.hello.result.Result;
import com.hello.service.OrderInfoService;
import com.hello.vo.order.OrderSubmitVo;
import com.hello.vo.order.OrderUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "订单功能接口")
@RestController
@RequestMapping("/api/order/")
public class OrderController {
    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("确认订单")
    @GetMapping("auth/confirmOrder")
    public Result confirm() {
        Long userId = AuthContextHolder.getUserId();
        return orderInfoService.confirmOrder(userId);
    }
    @ApiOperation(value = "生成订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderSubmitVo orderSubmitVo){
        Long userId = AuthContextHolder.getUserId();
        return orderInfoService.submitOrder(orderSubmitVo,userId);
    }
    @ApiOperation(value = "获取订单详情信息")
    @GetMapping("auth/getOrderInfoById/{orderId}")
    public Result getOrderInfoById(@PathVariable Long orderId){
        return orderInfoService.getOrderInfoById(orderId);
    }

    @ApiOperation(value = "获取订单信息(远程调用,支付时使用)")
    @GetMapping("/inner/getOrderInfo/{orderNo}")
    public OrderInfo getOderInfoByOrderNo(@PathVariable String orderNo){
        return orderInfoService.getOderInfoByOrderNo(orderNo);
    }
    @ApiOperation(value = "获取用户订单分页列表")
    @GetMapping("auth/findUserOrderPage/{page}/{limit}")
    public Result findUserOrderPage(@PathVariable Long page, @PathVariable Long limit, OrderUserQueryVo orderUserQueryVo){
        Long userId = AuthContextHolder.getUserId();
        orderUserQueryVo.setUserId(userId);
        return orderInfoService.findUserOrderPage(page,limit,orderUserQueryVo);
    }
}
