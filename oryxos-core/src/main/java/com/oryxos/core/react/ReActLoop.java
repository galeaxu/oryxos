package com.oryxos.core.react;

import com.oryxos.core.memory.MemoryService;
import com.oryxos.core.profile.Profile;
import com.oryxos.core.provider.ProviderService;
import com.oryxos.core.session.ChatMessage;
import com.oryxos.core.session.Session;

/**
 * Agent 的核心循环引擎。输入 Session 和用户消息，输出最终响应。
 *
 * <p>ReAct 是 Reason 加 Act 的简称，算法见技术方案 4.1：
 * <ol>
 *     <li>接到用户消息追加到 Session 对话历史；</li>
 *     <li>组装 Prompt；</li>
 *     <li>调用 LLM Provider 获取响应；</li>
 *     <li>如果响应没有 Tool 调用，返回最终响应；</li>
 *     <li>如果有 Tool 调用，执行 Tool 并把结果作为 tool 消息追加到对话历史；</li>
 *     <li>回到组装 Prompt 步骤继续循环；</li>
 *     <li>达到最大迭代次数（默认 10 次）强制结束。</li>
 * </ol>
 *
 * <p><b>实现说明（US-2 阶段完成）</b>：调用 Spring AI 时只使用其协议转换与 schema 生成，
 * 必须禁用其自动 tool 执行，Tool 的实际调度由本循环配合 ToolExecutor 控制（宪章原则四）。
 */
public class ReActLoop {

    /** 最大迭代次数默认值，可在 Profile 的 settings.max_iterations 覆盖。 */
    public static final int DEFAULT_MAX_ITERATIONS = 10;

    private final ProviderService providerService;
    private final MemoryService memoryService;
    private final ToolExecutor toolExecutor;

    public ReActLoop(ProviderService providerService,
                     MemoryService memoryService,
                     ToolExecutor toolExecutor) {
        this.providerService = providerService;
        this.memoryService = memoryService;
        this.toolExecutor = toolExecutor;
    }

    /**
     * 执行一次完整的 ReAct 循环。
     *
     * <p>骨架阶段仅实现最小可用路径：组装 prompt → 调用 LLM → 返回响应。
     * Tool 调用解析与循环控制将在 US-2 阶段补齐。
     *
     * @param profile 当前 Agent 的 Profile
     * @param session 会话上下文
     * @param userMessage 用户消息
     * @return Agent 最终响应
     */
    public String run(Profile profile, Session session, String userMessage) {
        session.appendMessage(ChatMessage.user(userMessage));

        int maxIterations = profile.getSettings() != null
                ? profile.getSettings().getMaxIterations()
                : DEFAULT_MAX_ITERATIONS;

        String prompt = buildPrompt(profile, session);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            String response = providerService.call(profile, prompt);
            // TODO(US-2): 解析 response 中的 Tool 调用；若有则执行 Tool、回填结果并继续循环。
            session.appendMessage(ChatMessage.assistant(response));
            return response;
        }
        throw new IllegalStateException("ReAct 循环达到最大迭代次数：" + maxIterations);
    }

    private String buildPrompt(Profile profile, Session session) {
        String identity = profile.getIdentity() != null && profile.getIdentity().getPrompt() != null
                ? profile.getIdentity().getPrompt()
                : "";
        String memory = memoryService.loadContext(session);
        return identity + System.lineSeparator() + memory;
    }

    // getters 便于后续子模块（PromptBuilder / ToolExecutor 实现）复用依赖
    public ProviderService getProviderService() {
        return providerService;
    }

    public MemoryService getMemoryService() {
        return memoryService;
    }

    public ToolExecutor getToolExecutor() {
        return toolExecutor;
    }
}
