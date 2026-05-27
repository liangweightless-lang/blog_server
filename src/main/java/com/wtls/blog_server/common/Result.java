package com.wtls.blog_server.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result() {}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return success(data, ResultCode.OK.getMessage());
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.OK.getCode(), message, data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> clientFailed(String message) {
        return new Result<>(ResultCode.INVALID_REQUEST.getCode(), message, null);
    }

    public static <T> Result<T> serverFailed(String message) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(ResultCode.UNAUTHORIZED.getCode(), message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return serverFailed(message);
    }
}
