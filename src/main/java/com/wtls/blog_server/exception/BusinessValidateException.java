package com.wtls.blog_server.exception;

public class BusinessValidateException extends RuntimeException {
    public BusinessValidateException(String message) {
        super(message);
    }
}
