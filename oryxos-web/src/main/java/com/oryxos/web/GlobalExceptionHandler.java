package com.oryxos.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一异常处理，把异常转成标准 JSON 错误响应（code、message、timestamp）。见技术方案 7.1。
 *
 * <p>覆盖 400 / 404 / 405 / 500 / 503 等常见场景，统一使用 {@link ApiResponse} 结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400：请求参数非法。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, 400, "BAD_REQUEST", e.getMessage());
    }

    /** 409：状态冲突。 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, 409, "ILLEGAL_STATE", e.getMessage());
    }

    /** 404：资源不存在（静态资源未匹配）。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, 404, "NOT_FOUND", "资源不存在");
    }

    /** 404：处理器不存在。 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException e) {
        return build(HttpStatus.NOT_FOUND, 404, "NOT_FOUND", "接口不存在: " + e.getRequestURL());
    }

    /** 405：方法不允许。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, 405, "METHOD_NOT_ALLOWED", e.getMessage());
    }

    /** 按 ResponseStatusException 携带的状态码处理（含 503 等）。 */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            status = 500;
        }
        return build(httpStatus, status, httpStatus.name(), e.getReason());
    }

    /** 500：兜底。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternal(Exception e) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, 500, "INTERNAL_ERROR", e.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, int code, String defaultMessage, String message) {
        String msg = (message == null || message.isBlank()) ? defaultMessage : message;
        return ResponseEntity.status(status).body(ApiResponse.error(code, msg));
    }
}
