package com.hello.service;

import com.hello.result.Result;

import java.util.Map;

public interface WeChatService {
    /**
     * 下单 小程序支付
     * @param orderNo
     * @return
     */
    Result createJsapi(String orderNo);

    /**
     * 根据订单标识调用微信第三方接口获取支付状态
     * @param orderNo
     * @return
     */
    Map<String, String> queryPayStatus(String orderNo);
}
