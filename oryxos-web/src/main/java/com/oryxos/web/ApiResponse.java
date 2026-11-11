package com.oryxos.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * 统一响应体（见技术方案 7.1）。
 *
 * <p>所有 REST 接口统一以该结构返回：{@code code} / {@code message} / {@code data} / {@code timestamp}。
 * <ul>
 *     <li>{@code code}：业务状态码。成功为 {@code 0}，失败为非 0（如 {@code 400}、{@code 500}）。</li>
 *     <li>{@code message}：人类可读信息，成功一般为 {@code "ok"}。</li>
 *     <li>{@code data}：业务数据，失败时为 {@code null}（序列化时省略）。</li>
 *     <li>{@code timestamp}：响应生成时间（ISO-8601）。</li>
 * </ul>
 *
 * @param <T> 业务数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String message, T data, String timestamp) {

    /** 成功码。 */
    public static final int SUCCESS_CODE = 0;

    /** 成功响应（带数据）。 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS_CODE, "ok", data, Instant.now().toString());
    }

    /** 成功响应（无数据）。 */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(SUCCESS_CODE, "ok", null, Instant.now().toString());
    }

    /** 失败响应（自定义业务码与信息，无数据）。 */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message == null ? "" : message, null, Instant.now().toString());
    }
}
