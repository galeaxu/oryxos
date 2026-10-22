package com.oryxos.core.tool;

/**
 * OryxOS 内部统一的 Tool 抽象接口。
 *
 * <p>内置 Tool、@Tool 注解的 Plugin Tool、MCP Tool 都被包装成 OryxTool 实例注册到 ToolRegistry，
 * ReAct 循环不感知具体 Tool 的来源。
 *
 * <p>见技术方案 6.1。
 */
public interface OryxTool {

    /**
     * Tool 的唯一名字。
     */
    String getName();

    /**
     * Tool 的功能描述，供 LLM 理解何时调用。
     */
    String getDescription();

    /**
     * 入参的 JSON Schema，供 Function Calling 使用。
     */
    String getInputSchema();

    /**
     * 执行 Tool，接收 JSON 输入返回 {@link ToolResult}。
     */
    ToolResult execute(String jsonInput);
}
