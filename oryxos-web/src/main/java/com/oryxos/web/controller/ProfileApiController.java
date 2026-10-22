package com.oryxos.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Profile 查询 Controller：GET /profiles。见技术方案 7.2。
 */
@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileApiController {

    @GetMapping
    public List<String> list() {
        // TODO(US-5): 从 ProfileRegistry 返回所有 Profile 名。
        return List.of();
    }
}
