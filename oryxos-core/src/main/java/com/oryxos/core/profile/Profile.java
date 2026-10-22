package com.oryxos.core.profile;

import java.util.List;

/**
 * 一个 Agent 的完整配置（一个 Profile 对应一个 Agent）。
 *
 * <p>由 YAML 文件描述，字段见技术方案 8.2：
 * name、description、identity、provider、tools、skills、mcp_servers、channels、bootstrap、settings。
 */
public class Profile {

    private String name;
    private String description;
    private Identity identity;
    private ProviderConfig provider;
    private List<String> tools = List.of();
    private List<String> skills = List.of();
    private List<String> mcpServers = List.of();
    private List<String> channels = List.of();
    private List<String> bootstrap = List.of();
    private Settings settings = new Settings();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public ProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getMcpServers() {
        return mcpServers;
    }

    public void setMcpServers(List<String> mcpServers) {
        this.mcpServers = mcpServers;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public List<String> getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(List<String> bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * identity 段：Agent 名称、描述、人格 prompt（或引用 SOUL.md）。
     */
    public static class Identity {
        private String agentName;
        private String prompt;

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    /**
     * provider 段：绑定的 LLM Provider，provider 名加模型加参数。
     */
    public static class ProviderConfig {
        private String name;
        private String model;
        private Double temperature;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }

    /**
     * settings 段：max_iterations、max_history_turns。
     */
    public static class Settings {
        private int maxIterations = 10;
        private int maxHistoryTurns = 20;

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public int getMaxHistoryTurns() {
            return maxHistoryTurns;
        }

        public void setMaxHistoryTurns(int maxHistoryTurns) {
            this.maxHistoryTurns = maxHistoryTurns;
        }
    }
}
