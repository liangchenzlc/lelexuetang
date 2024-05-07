package com.lelexuetang.base.exception;

public enum CommonError {
    PARAMS_IS_NULL("参数为空"),
    PARAMS_IS_BLANK("参数为空"),
    PARAMS_IS_INVALID("参数不合法"),
    OBJECT_NOT_FOUND("对象不存在"),
    UNKNOWN_ERROR("未知错误，请重试"),
    REQUEST_NOT_FOUND("请求不存在"),
    REQUEST_FORBIDDEN("请求被拒绝"),
    REQUEST_TIMEOUT("请求超时"),
    REQUEST_TOO_MANY("请求次数过多"),
    REQUEST_METHOD_NOT_SUPPORTED("请求方法不支持");

    //错误信息
    private String errMessage;
    public String getErrMessage() {
        return errMessage;
    }
    private CommonError(String errMessage) {
        this.errMessage = errMessage;
    }
}
