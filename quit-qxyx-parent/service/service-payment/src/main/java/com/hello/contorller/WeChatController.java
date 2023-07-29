package com.hello.contorller;

import com.hello.enums.PaymentType;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.PaymentInfoService;
import com.hello.service.WeChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/payment/weixin")
@Slf4j
public class WeChatController {
    @Resource
    private WeChatService weChatService;
    @Resource
    private PaymentInfoService paymentInfoService;

    @ApiOperation(value = "下单 小程序支付")
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(@PathVariable String orderNo){
        return weChatService.createJsapi(orderNo);
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(@PathVariable String orderNo){
        //根据订单标识调用微信第三方接口获取支付状态
        Map<String, String> resultMap = weChatService.queryPayStatus(orderNo);
        if (resultMap == null) {//支付失败
            return Result.fail(ResultCodeEnum.ORDER_FAIL);
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//如果成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(orderNo, PaymentType.WEIXIN, resultMap);
            return Result.ok(ResultCodeEnum.ORDER_SUCESS);
        }
        return Result.ok(ResultCodeEnum.ORDER_ING);
    }
}