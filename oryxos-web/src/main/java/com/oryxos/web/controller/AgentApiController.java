package com.oryxos.web.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Agent 调用 Controller：无状态调用一次 Agent。见技术方案 7.2。
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentApiController {

    @PostMapping("/{name}/invoke")
    public Map<String, Object> invoke(@PathVariable String name,
                                      @RequestBody Map<String, Object> body) {
        // TODO(US-5): 按 name 取 Profile，构造临时 Session，调 AgentService.process 后返回。
        return Map.of("agent", name, "reply", "placeholder");
    }
}
