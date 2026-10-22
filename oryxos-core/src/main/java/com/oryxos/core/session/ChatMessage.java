package com.oryxos.core.session;

import java.time.Instant;

/**
 * 对话历史中的一条消息。
 *
 * <p>对应需求文档数据模型：每条消息有 role、content、timestamp、tool_calls。
 *
 * @param role      角色（system / user / assistant / tool）
 * @param content   消息内容
 * @param timestamp 时间戳
 */
public record ChatMessage(String role, String content, Instant timestamp) {

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, Instant.now());
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, Instant.now());
    }

    public static ChatMessage tool(String content) {
        return new ChatMessage("tool", content, Instant.now());
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, Instant.now());
    }
}
