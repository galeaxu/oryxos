package com.oryxos.web.controller;

import com.oryxos.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Memory 查询 Controller：GET /memory。见技术方案 7.2。
 */
@RestController
@RequestMapping("/api/v1/memory")
public class MemoryApiController {

    @GetMapping
    public ApiResponse<Map<String, Object>> get() {
        // TODO(US-5): 从 MemoryService 读取长期记忆并返回。
        return ApiResponse.ok(Map.of("content", ""));
    }
}