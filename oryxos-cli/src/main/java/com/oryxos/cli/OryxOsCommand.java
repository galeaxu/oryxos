package com.oryxos.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Picocli 根命令。整个 OryxOS 的命令行入口，注册 12 个子命令（见技术方案 8.6）。
 *
 * <p>骨架阶段先挂载子命令占位，US-1 / US-5 阶段逐个实现。
 */
@Command(
        name = "oryxos",
        mixinStandardHelpOptions = true,
        version = "OryxOS 1.0.0-SNAPSHOT",
        description = "企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座",
        subcommands = {
                OryxOsCommand.InitCommand.class,
                OryxOsCommand.StatusCommand.class,
                OryxOsCommand.ChatCommand.class,
                OryxOsCommand.ServeCommand.class,
                OryxOsCommand.GatewayCommand.class,
                OryxOsCommand.ProfileCommand.class,
                OryxOsCommand.ProviderCommand.class,
                OryxOsCommand.ToolCommand.class,
                OryxOsCommand.SessionCommand.class
        }
)
public class OryxOsCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /** oryxos init：初始化工作区。 */
    @Command(name = "init", description = "初始化 .oryxos/ 工作区")
    static class InitCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos init 将在 US-2 阶段实现");
            return 0;
        }
    }

    /** oryxos status：查看配置和运行状态。 */
    @Command(name = "status", description = "查看配置和运行状态")
    static class StatusCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos status 将在 US-5 阶段实现");
            return 0;
        }
    }

    /** oryxos chat：交互对话。 */
    @Command(name = "chat", description = "交互式多轮对话")
    static class ChatCommand implements Callable<Integer> {
        @Option(names = "--profile", description = "指定 Profile", defaultValue = "default")
        String profile;

        @Option(names = "--message", description = "发送单条消息后退出")
        String message;

        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos chat 将在 US-2 阶段实现（profile=" + profile + "）");
            return 0;
        }
    }

    /** oryxos serve：启动 HTTP API 服务。 */
    @Command(name = "serve", description = "启动 HTTP API 服务")
    static class ServeCommand implements Callable<Integer> {
        @Option(names = "--port", description = "端口", defaultValue = "8080")
        int port;

        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos serve 将在 US-5 阶段实现（port=" + port + "）");
            return 0;
        }
    }

    /** oryxos gateway：多渠道守护进程。 */
    @Command(name = "gateway", description = "启动多渠道守护进程")
    static class GatewayCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos gateway 将在 US-5 阶段实现");
            return 0;
        }
    }

    /** oryxos profile：Profile 管理。 */
    @Command(name = "profile", description = "Profile 管理",
            subcommands = {ProfileListCommand.class, ProfileCreateCommand.class,
                    ProfileShowCommand.class, ProfileDeleteCommand.class})
    static class ProfileCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "list", description = "列出所有 Profile")
    static class ProfileListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos profile list");
            return 0;
        }
    }

    @Command(name = "create", description = "创建新 Profile")
    static class ProfileCreateCommand implements Callable<Integer> {
        @CommandLine.Parameters(index = "0", description = "Profile 名")
        String name;

        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos profile create " + name);
            return 0;
        }
    }

    @Command(name = "show", description = "查看 Profile 详情")
    static class ProfileShowCommand implements Callable<Integer> {
        @CommandLine.Parameters(index = "0", description = "Profile 名")
        String name;

        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos profile show " + name);
            return 0;
        }
    }

    @Command(name = "delete", description = "删除 Profile")
    static class ProfileDeleteCommand implements Callable<Integer> {
        @CommandLine.Parameters(index = "0", description = "Profile 名")
        String name;

        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos profile delete " + name);
            return 0;
        }
    }

    /** oryxos provider：Provider 查询。 */
    @Command(name = "provider", description = "Provider 查询",
            subcommands = {ProviderListCommand.class})
    static class ProviderCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "list", description = "列出已配置的 Provider")
    static class ProviderListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos provider list");
            return 0;
        }
    }

    /** oryxos tool：Tool 查询。 */
    @Command(name = "tool", description = "Tool 查询",
            subcommands = {ToolListCommand.class})
    static class ToolCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "list", description = "列出已注册的 Tool")
    static class ToolListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos tool list");
            return 0;
        }
    }

    /** oryxos session：Session 查询。 */
    @Command(name = "session", description = "Session 查询",
            subcommands = {SessionListCommand.class})
    static class SessionCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            CommandLine.usage(this, System.out);
            return 0;
        }
    }

    @Command(name = "list", description = "列出会话历史")
    static class SessionListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("[TODO] oryxos session list");
            return 0;
        }
    }
}
