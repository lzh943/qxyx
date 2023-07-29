package com.hello.contorller;

import com.hello.activity.ActivityFeignClient;
import com.hello.auth.AuthContextHolder;
import com.hello.model.order.CartInfo;
import com.hello.result.Result;
import com.hello.service.CartInfoService;
import com.hello.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    @Resource
    private CartInfoService cartInfoService;
    @Resource
    private ActivityFeignClient activityFeignClient;

    @ApiOperation(value = "根据skuId更新选中信息")
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,@PathVariable Integer isChecked){
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.checkCart(userId,skuId,isChecked);
    }
    @ApiOperation(value = "对购物项全部选中")
    @GetMapping("checkAllCart/{isChecked}")
    public Result checkAllCart(@PathVariable Integer isChecked) {
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.checkAllCart(userId, isChecked);

    }
    @ApiOperation(value="批量选择购物项")
    @PostMapping("batchCheckCart/{isChecked}")
    public Result batchCheckCart(@PathVariable Integer isChecked,@RequestBody List<Long> skuIdList){
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.batchCHeckCart(userId,skuIdList,isChecked);
    }
    @ApiOperation(value = "查看购物车列表")
    @GetMapping("/cartList")
    public Result cartList(){
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.getCartListByUserId(userId);
    }
    @ApiOperation(value = "根据活动规则和优惠劵对购物项进行封装")
    @GetMapping("activityCartList")
    public Result activityCartList() {
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        OrderConfirmVo orderTradeVo = activityFeignClient.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderTradeVo);
    }
    @ApiOperation(value = "添加商品到购物车")
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,@PathVariable Integer skuNum){
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.addToCart(userId,skuId,skuNum);
    }
    @ApiOperation(value="删除商品")
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId) {
        Long userId = AuthContextHolder.getUserId();
        return cartInfoService.deleteCart(skuId, userId);
    }
    @ApiOperation(value="批量删除购物车")
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        return  cartInfoService.batchDeleteCart(skuIdList, userId);
    }
    @ApiOperation(value="清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        return  cartInfoService.deleteAllCart(userId);
    }
    //获取当前用户选中的购物项(远程调用)
    @GetMapping("inner/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId) {
        return cartInfoService.getCartCheckedList(userId);
    }
}
