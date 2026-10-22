package com.oryxos.tool.sandbox;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * Tool 执行前的白名单校验。三个核心方法见技术方案 6.7。
 *
 * <p><b>注意</b>：不使用 Java SecurityManager（JDK 21 已不可用），采用应用层 Path/Pattern 白名单。
 */
public class SandboxChecker {

    private final List<String> allowedPaths;
    private final List<String> allowedCommands;
    private final List<String> allowedDomains;

    public SandboxChecker(List<String> allowedPaths,
                          List<String> allowedCommands,
                          List<String> allowedDomains) {
        this.allowedPaths = List.copyOf(allowedPaths);
        this.allowedCommands = List.copyOf(allowedCommands);
        this.allowedDomains = List.copyOf(allowedDomains);
    }

    /**
     * 路径标准化后比对白名单。
     */
    public void checkFilePath(String path) {
        Path normalized = Path.of(path).toAbsolutePath().normalize();
        boolean allowed = allowedPaths.stream()
                .map(p -> Path.of(p).toAbsolutePath().normalize())
                .anyMatch(normalized::startsWith);
        if (!allowed) {
            throw new SandboxViolationException("文件路径不在白名单内：" + path);
        }
    }

    /**
     * 拆出命令首个 token 比对白名单。
     */
    public void checkShellCommand(String command) {
        if (command == null || command.isBlank()) {
            throw new SandboxViolationException("空命令");
        }
        String firstToken = command.trim().split("\\s+")[0];
        if (!allowedCommands.contains(firstToken)) {
            throw new SandboxViolationException("命令不在白名单内：" + firstToken);
        }
    }

    /**
     * 解析 host 后做通配符匹配。
     */
    public void checkHttpUrl(String url) {
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException e) {
            throw new SandboxViolationException("非法 URL：" + url);
        }
        if (host == null) {
            throw new SandboxViolationException("无法解析域名：" + url);
        }
        boolean allowed = allowedDomains.stream().anyMatch(pattern -> matchesDomain(host, pattern));
        if (!allowed) {
            throw new SandboxViolationException("域名不在白名单内：" + host);
        }
    }

    private boolean matchesDomain(String host, String pattern) {
        if (pattern.startsWith("*.")) {
            String suffix = pattern.substring(1); // ".example.com"
            return host.endsWith(suffix) || host.equals(pattern.substring(2));
        }
        return host.equals(pattern);
    }

    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains;
    }
}
