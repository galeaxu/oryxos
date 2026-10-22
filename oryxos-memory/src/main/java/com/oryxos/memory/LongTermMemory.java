package com.oryxos.memory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长期记忆的核心读写，底层操作 {@code .oryxos/memory/MEMORY.md} 一个 Markdown 文件。
 *
 * <p>对外提供四个方法：append、load、recallByKeyword、truncateIfNeeded。见技术方案 5.1。
 *
 * <p>接口预留向量检索升级空间：{@link #recallByKeyword(String)} 设计成可升级为带 mode 参数的 recall，
 * 切换底层实现不影响上层。
 */
public class LongTermMemory {

    /** 超过此字数保留最近内容。见技术方案 5.1。 */
    public static final int MAX_CHARS = 4000;

    private final Path memoryFile;

    public LongTermMemory(Path memoryFile) {
        this.memoryFile = memoryFile;
    }

    /**
     * 追加内容，自动加日期 header。
     */
    public void append(String content) throws IOException {
        Files.createDirectories(memoryFile.getParent());
        String entry = "## " + LocalDate.now() + System.lineSeparator()
                + content + System.lineSeparator() + System.lineSeparator();
        Files.writeString(memoryFile, entry, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * 加载整个文件，超阈值截断。
     */
    public String load() throws IOException {
        if (!Files.exists(memoryFile)) {
            return "";
        }
        String content = Files.readString(memoryFile, StandardCharsets.UTF_8);
        return truncateIfNeeded(content);
    }

    /**
     * 按关键词检索返回匹配行（骨架阶段采用简单 includes 匹配，扩展阶段升级为向量检索）。
     */
    public String recallByKeyword(String query) throws IOException {
        if (!Files.exists(memoryFile) || query == null || query.isBlank()) {
            return "";
        }
        List<String> lines = Files.readAllLines(memoryFile, StandardCharsets.UTF_8);
        return lines.stream()
                .filter(line -> line.contains(query))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * 超过 {@link #MAX_CHARS} 字保留最近内容。
     */
    public String truncateIfNeeded(String content) {
        if (content == null || content.length() <= MAX_CHARS) {
            return content;
        }
        return content.substring(content.length() - MAX_CHARS);
    }

    public Path getMemoryFile() {
        return memoryFile;
    }
}
