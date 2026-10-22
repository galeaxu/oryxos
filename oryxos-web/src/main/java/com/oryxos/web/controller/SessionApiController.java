package com.oryxos.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 会话管理 Controller：创建、发消息、查历史、归档。见技术方案 7.2。
 *
 * <p>骨架阶段返回占位响应；US-5 阶段接入 AgentService 与 SessionRepository。
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionApiController {

    @PostMapping
    public Map<String, Object> createSession(@RequestBody(required = false) Map<String, Object> body) {
        // TODO(US-5): 生成 session_id（channel + user + profile），落库后返回。
        return Map.of("sessionId", "placeholder-session-id");
    }

    @PostMapping("/{id}/messages")
    public Map<String, Object> sendMessage(@PathVariable String id,
                                           @RequestBody Map<String, Object> body) {
        // TODO(US-5): 调 AgentService.process 走完整 ReActLoop 后返回响应。
        return Map.of("sessionId", id, "reply", "placeholder");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getHistory(@PathVariable String id) {
        // TODO(US-5): 从 SessionRepository 读取 messages_json 并返回。
        return Map.of("sessionId", id, "messages", java.util.List.of());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable String id) {
        // TODO(US-5): 将 Session 状态置为 archived。
        return ResponseEntity.noContent().build();
    }
}
