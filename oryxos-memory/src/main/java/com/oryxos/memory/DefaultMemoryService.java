package com.oryxos.memory;

import com.oryxos.core.memory.MemoryService;
import com.oryxos.core.session.ChatMessage;
import com.oryxos.core.session.Session;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * MemoryService 三层记忆统一门面的默认实现。
 *
 * <p>内部把长期记忆委托给 {@link LongTermMemory}；会话记忆在骨架阶段直接从 {@link Session} 读取
 * （US-5 阶段会话记忆将由 oryxos-storage 的 SessionManager 经 SQLite 持久化）。
 */
public class DefaultMemoryService implements MemoryService {

    private final LongTermMemory longTermMemory;

    public DefaultMemoryService(LongTermMemory longTermMemory) {
        this.longTermMemory = longTermMemory;
    }

    @Override
    public String loadContext(Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("[会话历史]").append(System.lineSeparator());
        if (session != null) {
            for (ChatMessage message : session.getMessages()) {
                sb.append(message.role()).append(": ").append(message.content())
                        .append(System.lineSeparator());
            }
        }
        sb.append("[长期记忆]").append(System.lineSeparator());
        try {
            sb.append(longTermMemory.load());
        } catch (IOException e) {
            throw new UncheckedIOException("加载长期记忆失败", e);
        }
        return sb.toString();
    }

    @Override
    public void appendLongTerm(String content) {
        try {
            longTermMemory.append(content);
        } catch (IOException e) {
            throw new UncheckedIOException("写入长期记忆失败", e);
        }
    }

    @Override
    public String recall(String query) {
        try {
            return longTermMemory.recallByKeyword(query);
        } catch (IOException e) {
            throw new UncheckedIOException("检索长期记忆失败", e);
        }
    }
}
