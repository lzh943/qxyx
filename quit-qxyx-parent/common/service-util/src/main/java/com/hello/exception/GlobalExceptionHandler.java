package com.hello.exception;

import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
//AOP 面向切面
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.build(null, ResultCodeEnum.SERVICE_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(SysException.class)
    @ResponseBody
    public Result error(SysException e){
        return Result.build(null,e.getCode(),e.getMessage());
    }
}