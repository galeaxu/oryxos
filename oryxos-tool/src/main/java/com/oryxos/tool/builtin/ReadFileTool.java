package com.oryxos.tool.builtin;

import com.oryxos.core.tool.OryxTool;
import com.oryxos.core.tool.ToolResult;
import com.oryxos.tool.sandbox.SandboxChecker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 内置文件读 Tool（read_file）。执行前调 SandboxChecker 做路径白名单检查。见技术方案 6.2。
 */
public class ReadFileTool implements OryxTool {

    private final SandboxChecker sandboxChecker;

    public ReadFileTool(SandboxChecker sandboxChecker) {
        this.sandboxChecker = sandboxChecker;
    }

    @Override
    public String getName() {
        return "read_file";
    }

    @Override
    public String getDescription() {
        return "读取指定路径文件的内容";
    }

    @Override
    public String getInputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "path": { "type": "string", "description": "文件路径" }
                  },
                  "required": ["path"]
                }
                """;
    }

    @Override
    public ToolResult execute(String jsonInput) {
        // TODO(US-4): 从 jsonInput 解析 path 字段（接入 JSON 库后完善）。
        try {
            String path = jsonInput;
            sandboxChecker.checkFilePath(path);
            String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
            return ToolResult.ok(content);
        } catch (IOException e) {
            return ToolResult.fail("读取文件失败：" + e.getMessage(), true);
        }
    }
}
