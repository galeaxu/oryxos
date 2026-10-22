package com.oryxos.core.provider;

import com.oryxos.core.profile.Profile;

/**
 * 统一管理所有 LLM Provider，对 ReAct 循环屏蔽不同 LLM 厂商的差异。
 *
 * <p>ReAct 循环调 LLM 时传入 Profile 和 Prompt，ProviderService 按 Profile 配置选对应的底层
 * ChatModel 完成调用。见技术方案 3.1。
 *
 * <p>实现位于 {@code oryxos-provider} 模块。
 */
public interface ProviderService {

    /**
     * 调用 LLM 获取响应。
     *
     * @param profile 当前 Agent 的 Profile（含 provider name、model、temperature）
     * @param prompt  已组装好的 Prompt
     * @return LLM 响应文本
     */
    String call(Profile profile, String prompt);
}
