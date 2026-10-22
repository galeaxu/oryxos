package com.oryxos.cli;

import picocli.CommandLine;

/**
 * Picocli 命令行入口，整个 OryxOS 的 main 函数（技术方案 8.6）。
 *
 * <p>不需要 Spring 上下文的命令（init、profile list）直接走文件操作启动快；
 * 需要 LLM 调用的命令（chat、serve、gateway）启动 Spring 上下文。骨架阶段统一走 Picocli 入口。
 */
public final class OryxOsCli {

    private OryxOsCli() {
    }

    public static int run(String[] args) {
        return new CommandLine(new OryxOsCommand()).execute(args);
    }
}
