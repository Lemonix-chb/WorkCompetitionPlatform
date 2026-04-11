package com.example.workcompetitionplatform.config;

import com.example.workcompetitionplatform.dto.ApiResponse;
import com.example.workcompetitionplatform.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各种异常，返回标准的API响应格式
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return API响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常（@Valid注解）
     *
     * @param e 参数验证异常
     * @return API响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 提取所有验证错误信息
        String errorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error("参数验证失败：{}", errorMessage);
        return ApiResponse.badRequest(errorMessage);
    }

    /**
     * 处理约束违反异常
     *
     * @param e 约束违反异常
     * @return API响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        // 提取所有约束违反信息
        String errorMessage = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.error("约束违反：{}", errorMessage);
        return ApiResponse.badRequest(errorMessage);
    }

    /**
     * 处理请求参数缺失异常
     *
     * @param e 请求参数缺失异常
     * @return API响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String errorMessage = String.format("缺少必要的请求参数：%s", e.getParameterName());
        log.error(errorMessage);
        return ApiResponse.badRequest(errorMessage);
    }

    /**
     * 处理请求体不可读异常
     *
     * @param e 请求体不可读异常
     * @return API响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求体不可读：{}", e.getMessage());
        return ApiResponse.badRequest("请求体格式错误或不可读");
    }

    /**
     * 处理方法参数类型不匹配异常
     *
     * @param e 方法参数类型不匹配异常
     * @return API响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("参数 '%s' 类型错误，期望类型：%s",
                e.getName(),
                e.getRequiredType().getSimpleName());
        log.error(errorMessage);
        return ApiResponse.badRequest(errorMessage);
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return API响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String errorMessage = String.format("不支持请求方法：%s，支持的方法：%s",
                e.getMethod(),
                e.getSupportedHttpMethods().stream().map(HttpMethod::name).collect(java.util.stream.Collectors.joining(", ")));
        log.error(errorMessage);
        return ApiResponse.error(405, errorMessage);
    }

    /**
     * 处理404异常
     *
     * @param e 未找到处理器异常
     * @return API响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        String errorMessage = String.format("请求路径不存在：%s", e.getRequestURL());
        log.error(errorMessage);
        return ApiResponse.notFound(errorMessage);
    }

    /**
     * 处理认证异常
     *
     * @param e 认证异常
     * @return API响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证失败：{}", e.getMessage());
        return ApiResponse.unauthorized("认证失败：" + e.getMessage());
    }

    /**
     * 处理凭证错误异常
     *
     * @param e 凭证错误异常
     * @return API响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.error("凭证错误：{}", e.getMessage());
        return ApiResponse.unauthorized("用户名或密码错误");
    }

    /**
     * 处理访问拒绝异常
     *
     * @param e 访问拒绝异常
     * @return API响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问被拒绝：{}", e.getMessage());
        return ApiResponse.forbidden("权限不足，无法访问");
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常
     * @return API响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ApiResponse.error("系统异常，请联系管理员");
    }
}