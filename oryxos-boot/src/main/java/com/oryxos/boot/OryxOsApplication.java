package com.oryxos.boot;

import com.oryxos.cli.OryxOsCli;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OryxOS 启动入口。
 *
 * <p>启动策略（见技术方案 8.6）：
 * <ul>
 *     <li>无参数，或首个参数是 {@code serve} / {@code gateway} —— 启动 Spring Boot（HTTP API 服务）；</li>
 *     <li>其他参数 —— 交给 Picocli 处理（init / chat / profile 等命令行子命令以及 --help）。</li>
 * </ul>
 *
 * <p>关键点：Picocli 处理完成后直接退出，不再启动 Spring，避免"CLI 命令执行完又拉起 Web 服务"，
 * 也避免 Spring 的配置项（如 --server.port=xxxx）被 Picocli 误当成未知选项而报错。
 */
@SpringBootApplication
public class OryxOsApplication {

    public static void main(String[] args) {
        // 确保工作目录存在，SQLite 数据文件得以落地（oryxos init 之前也能启动）
        ensureWorkspaceDirectory();

        if (shouldStartServer(args)) {
            // 去掉首位的 serve / gateway 子命令名，其余参数（如 --server.port=xxxx）透传给 Spring
            String[] springArgs = args.length > 0
                    ? java.util.Arrays.copyOfRange(args, 1, args.length)
                    : args;
            SpringApplication.run(OryxOsApplication.class, springArgs);
            return;
        }

        // 纯命令行模式：交给 Picocli，结束后退出，不再启动 Spring
        int exitCode = OryxOsCli.run(args);
        System.exit(exitCode);
    }

    /**
     * 判断是否应启动 Spring Boot Web 服务。
     *
     * <p>无参数启动服务；显式 {@code serve} / {@code gateway} 启动服务；
     * 其余情况都走 CLI 分支。{@code serve} 后面跟的 Spring 配置参数会原样透传给 Spring。
     */
    private static boolean shouldStartServer(String[] args) {
        if (args.length == 0) {
            return true;
        }
        String first = args[0];
        return "serve".equals(first) || "gateway".equals(first);
    }

    private static void ensureWorkspaceDirectory() {
        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), ".oryxos"));
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建工作目录 .oryxos/", e);
        }
    }
}

