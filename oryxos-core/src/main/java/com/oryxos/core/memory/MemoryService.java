package com.oryxos.core.memory;

import com.oryxos.core.session.Session;

/**
 * Memory 三层记忆的统一门面。
 *
 * <p>对 ReAct 循环只暴露一个接口，内部把会话记忆委托给 SessionManager、长期记忆委托给
 * LongTermMemory。ReAct 循环组装 prompt 时只调这一个接口拿到完整上下文。
 *
 * <p>见技术方案 5.1。实现位于 {@code oryxos-memory} 模块。
 */
public interface MemoryService {

    /**
     * 加载注入 system prompt 的记忆上下文（会话历史 + 长期记忆）。
     */
    String loadContext(Session session);

    /**
     * 追加一条长期记忆（内部委托 LongTermMemory.append）。
     */
    void appendLongTerm(String content);

    /**
     * 按关键词检索长期记忆。
     */
    String recall(String query);
}
