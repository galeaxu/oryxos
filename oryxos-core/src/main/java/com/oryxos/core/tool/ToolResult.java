package com.oryxos.core.tool;

/**
 * Tool 执行结果。
 *
 * <p>包含成功标识、结果内容、错误信息、是否可重试。见技术方案 6.1。
 *
 * @param success   是否成功
 * @param content   成功时的结果内容
 * @param error     失败时的错误信息
 * @param retryable 失败时是否可重试
 */
public record ToolResult(boolean success, String content, String error, boolean retryable) {

    public static ToolResult ok(String content) {
        return new ToolResult(true, content, null, false);
    }

    public static ToolResult fail(String error, boolean retryable) {
        return new ToolResult(false, null, error, retryable);
    }
}
