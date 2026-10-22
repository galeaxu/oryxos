package com.oryxos.tool.sandbox;

/**
 * Sandbox 白名单校验失败时抛出，Tool 执行终止。见技术方案 6.7。
 */
public class SandboxViolationException extends RuntimeException {

    public SandboxViolationException(String message) {
        super(message);
    }
}
