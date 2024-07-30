package com.lelexuetang.base.model;

import lombok.Data;

@Data
public class RestResponse<T> {
    private int code;
    private String msg;
    private T result;
    public RestResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.result = data;
    }
    public RestResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public RestResponse(T data) {
        this.code = 0;
        this.msg = "success";
        this.result = data;
    }

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(data);
    }
    public static <T> RestResponse<T> success() {
        return new RestResponse<>(null);
    }
    public static <T> RestResponse<T> error(String msg) {
        return new RestResponse<>(-1, msg);
    }
    public static <T> RestResponse<T> error(String msg, T data) {
        return new RestResponse<>(-1, msg, data);
    }
}
