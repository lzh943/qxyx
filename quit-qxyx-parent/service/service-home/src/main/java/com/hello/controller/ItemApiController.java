package com.hello.controller;

import com.hello.auth.AuthContextHolder;
import com.hello.result.Result;
import com.hello.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "商品详情")
@RestController
@RequestMapping("api/home")
public class ItemApiController {
    @Resource
    private ItemService itemService;

    @ApiOperation(value = "查询商品详细信息")
    @GetMapping("/item/{id}")
    public Result index(@PathVariable Long id){
        Long userId = AuthContextHolder.getUserId();
        return itemService.item(id,userId);
    }
}
