package com.oryxos.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户和 Agent 一次对话的上下文容器。
 *
 * <p>Session 标识由 Channel、用户、Agent（Profile）联合生成。见需求文档 5.9 与技术方案 9.2。
 */
public class Session {

    private final String sessionId;
    private final String profileName;
    private final String channel;
    private final String userId;

    private final List<ChatMessage> messages = new ArrayList<>();

    private SessionStatus status = SessionStatus.ACTIVE;

    private final Instant createdAt = Instant.now();
    private Instant lastActiveAt = Instant.now();

    public Session(String sessionId, String profileName, String channel, String userId) {
        this.sessionId = sessionId;
        this.profileName = profileName;
        this.channel = channel;
        this.userId = userId;
    }

    public void appendMessage(ChatMessage message) {
        this.messages.add(message);
        this.lastActiveAt = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getChannel() {
        return channel;
    }

    public String getUserId() {
        return userId;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void archive() {
        this.status = SessionStatus.ARCHIVED;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }
}
