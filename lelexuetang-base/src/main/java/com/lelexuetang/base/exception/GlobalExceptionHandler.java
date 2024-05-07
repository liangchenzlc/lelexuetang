package com.lelexuetang.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // 自定义异常
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LeLeXueTangException.class)
    public RestErrorResponse customExceptionHandler(LeLeXueTangException e) {
        // 打印异常信息
        log.error("【自定义异常】{}", e.getErrMessage(), e);
        // 统一返回
        return new RestErrorResponse(e.getErrMessage());
    }

    // 全局异常
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestErrorResponse defaultExceptionHandler(Exception e) {
        // 打印异常信息
        log.error("【全局异常】{}", e.getMessage(), e);
        // 统一返回
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse validExceptionHandler(MethodArgumentNotValidException e) {
        String errMessage = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(","));
        // 打印异常信息
        log.error("【参数校验异常】{}", errMessage, e);
        // 统一返回
        return new RestErrorResponse(errMessage);
    }
}
