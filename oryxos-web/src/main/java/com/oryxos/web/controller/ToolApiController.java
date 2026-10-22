package com.oryxos.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tool 信息 Controller：GET /tools。见技术方案 7.2。
 */
@RestController
@RequestMapping("/api/v1/tools")
public class ToolApiController {

    @GetMapping
    public List<String> list() {
        // TODO(US-5): 从 ToolRegistry 返回所有已注册 Tool 名。
        return List.of();
    }
}
