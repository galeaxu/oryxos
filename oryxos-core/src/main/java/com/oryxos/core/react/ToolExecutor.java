package com.oryxos.core.react;

import com.oryxos.core.tool.ToolResult;

/**
 * 执行 LLM 返回的 Tool 调用请求。
 *
 * <p>从 ToolRegistry 找到对应 Tool，做 Sandbox 检查，执行 Tool，把结果包装成 ToolResult 返回给
 * ReAct 循环，并写入 tool_invocations 表。见技术方案 4.2。
 */
public interface ToolExecutor {

    /**
     * 执行一次 Tool 调用。
     *
     * @param toolName Tool 名
     * @param jsonInput Tool 入参 JSON
     * @return 执行结果
     */
    ToolResult execute(String toolName, String jsonInput);
}
