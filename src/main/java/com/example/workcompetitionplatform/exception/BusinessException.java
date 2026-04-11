package com.example.workcompetitionplatform.exception;

/**
 * 业务异常类
 * 用于抛出业务逻辑相关的异常
 *
 * @author 陈海波
 * @since 2026-01-19
 */
public class BusinessException extends RuntimeException {

    /**
     * 异常状态码
     */
    private Integer code;

    /**
     * 默认构造函数
     */
    public BusinessException() {
        super();
        this.code = 500;
    }

    /**
     * 构造函数（仅消息）
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造函数（状态码和消息）
     *
     * @param code 异常状态码
     * @param message 异常消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数（消息和原因）
     *
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    /**
     * 构造函数（状态码、消息和原因）
     *
     * @param code 异常状态码
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取异常状态码
     *
     * @return 异常状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置异常状态码
     *
     * @param code 异常状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }
}