package com.oryxos.tool.registry;

import com.oryxos.core.tool.OryxTool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 统一管理所有 Tool。
 *
 * <p>启动时通过 Spring 容器扫描所有 @Tool 注解的方法（内置 Tool 和方式三的 Plugin Tool），
 * 加上 MCP Client 注册的工具（方式二），全部包装成 OryxTool 实例。Profile 启动 Agent 时按 tools
 * 字段从 Registry 过滤出该 Profile 可用的 Tool 子集。见技术方案 6.6。
 */
public class ToolRegistry {

    private final Map<String, OryxTool> tools = new LinkedHashMap<>();

    public void register(OryxTool tool) {
        tools.put(tool.getName(), tool);
    }

    public Optional<OryxTool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Set<String> names() {
        return tools.keySet();
    }

    public boolean contains(String name) {
        return tools.containsKey(name);
    }
}
