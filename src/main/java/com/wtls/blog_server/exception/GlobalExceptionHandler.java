package com.wtls.blog_server.exception;

import com.wtls.blog_server.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统发生未知异常", e);
        return Result.serverFailed("系统发生未知异常，请联系管理员");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) message = fieldError.getDefaultMessage();
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) message = fieldError.getDefaultMessage();
        }
        return Result.clientFailed(message);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.clientFailed(e.getMessage());
    }

    @ExceptionHandler(BusinessValidateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessValidateException(BusinessValidateException e) {
        log.warn("业务参数校验异常: {}", e.getMessage());
        return Result.clientFailed(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("发生未授权访问请求: {}", e.getMessage());
        return Result.unauthorized(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HTTP消息解析失败: {}", e.getMessage());
        return Result.clientFailed("请求参数格式错误");
    }
}
