package com.oryxos.provider;

import com.oryxos.core.profile.Profile;
import com.oryxos.core.provider.ProviderService;

import java.util.Map;

/**
 * ProviderService 的基础实现（骨架版）。
 *
 * <p><b>关键约定（技术方案 3.2）</b>：必须维护一份显式的 provider name 到 ChatModel 的映射，
 * 而不是靠"扫描容器里所有 ChatModel"自动区分——多 Provider 并存时 Bean 类型相同、Bean name
 * 未必等于 provider name，会有歧义。
 *
 * <p>US-1 阶段将在此接入 Spring AI Alibaba 的 ChatClient，并按 provider name 建立映射表。
 */
public class DefaultProviderService implements ProviderService {

    /**
     * 显式映射：provider name → ChatModel 引用。
     * 骨架阶段为空，US-1 阶段填充真实 ChatModel。
     */
    private final Map<String, String> providerToChatModel;

    public DefaultProviderService(Map<String, String> providerToChatModel) {
        this.providerToChatModel = Map.copyOf(providerToChatModel);
    }

    @Override
    public String call(Profile profile, String prompt) {
        String providerName = profile.getProvider() != null ? profile.getProvider().getName() : null;
        if (providerName == null) {
            throw new IllegalStateException("Profile 未配置 provider.name：" + profile.getName());
        }
        if (!providerToChatModel.containsKey(providerName)) {
            throw new IllegalStateException("未知的 Provider：" + providerName
                    + "，已注册的 Provider：" + providerToChatModel.keySet());
        }
        // TODO(US-1): 通过 Spring AI Alibaba 的 ChatClient 调用对应 ChatModel 并返回响应。
        throw new UnsupportedOperationException(
                "ProviderService.call 将在 US-1 阶段接入 Spring AI Alibaba 后实现，当前 provider=" + providerName);
    }

    public Map<String, String> getProviderToChatModel() {
        return providerToChatModel;
    }
}
