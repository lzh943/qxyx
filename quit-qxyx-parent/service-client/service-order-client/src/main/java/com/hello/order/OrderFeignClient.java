package com.hello.order;

import com.hello.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @GetMapping("api/order/inner/getOrderInfo/{orderNo}")
    OrderInfo getOderInfoByOrderNo(@PathVariable String orderNo);
}
