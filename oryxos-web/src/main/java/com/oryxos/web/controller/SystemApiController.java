package com.oryxos.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统状态 Controller：health、info。见技术方案 7.2。
 */
@RestController
@RequestMapping("/api/v1")
public class SystemApiController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "OryxOS",
                "version", "1.0.0-SNAPSHOT"
        );
    }
}
