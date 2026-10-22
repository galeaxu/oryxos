package com.oryxos.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * tool_invocations 表：每次 Tool 调用记录。
 *
 * <p>核心阶段就做写入（查询接口放扩展阶段），因为"可审计"是差异化卖点，审计数据地基应 day one 立起来。
 * 见技术方案 9.2。
 */
@Entity
@Table(name = "tool_invocations")
public class ToolInvocationEntity {

    @Id
    @Column(name = "invocation_id", nullable = false)
    private String invocationId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "status")
    private String status;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "token_cost")
    private Long tokenCost;

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getTokenCost() {
        return tokenCost;
    }

    public void setTokenCost(Long tokenCost) {
        this.tokenCost = tokenCost;
    }
}
