package com.oryxos.core.agent;

import com.oryxos.core.session.Session;

/**
 * Agent 对外的统一服务入口。
 *
 * <p>CLI Channel 和 Web Service 都通过它触发 Agent 处理一条消息，内部驱动 ReAct 循环。
 * 见技术方案 8.4 与 11 章关键流程。
 */
public interface AgentService {

    /**
     * 处理一条用户消息，返回 Agent 最终响应。
     *
     * @param session 会话上下文
     * @param message 用户消息
     * @return Agent 最终响应文本
     */
    String process(Session session, String message);
}
