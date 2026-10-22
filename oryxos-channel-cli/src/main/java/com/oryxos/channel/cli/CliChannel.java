package com.oryxos.channel.cli;

import com.oryxos.core.agent.AgentService;
import com.oryxos.core.profile.Profile;
import com.oryxos.core.session.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * CLI Channel：{@code oryxos chat} 命令的实现，读 stdin 写 stdout 实现交互式对话。
 *
 * <p>维护当前 Session，每次输入调 AgentService.process，支持 /quit 退出。见技术方案 8.4。
 */
public class CliChannel {

    private final AgentService agentService;
    private final Profile profile;
    private final BufferedReader in;
    private final PrintStream out;

    public CliChannel(AgentService agentService, Profile profile) {
        this(agentService, profile,
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out);
    }

    public CliChannel(AgentService agentService, Profile profile, BufferedReader in, PrintStream out) {
        this.agentService = agentService;
        this.profile = profile;
        this.in = in;
        this.out = out;
    }

    /**
     * 启动交互式对话循环。
     */
    public void start() throws IOException {
        Session session = new Session(
                UUID.randomUUID().toString(),
                profile.getName(),
                "cli",
                System.getProperty("user.name", "anonymous"));

        out.println("OryxOS chat 已启动（profile=" + profile.getName() + "），输入 /quit 退出。");
        while (true) {
            out.print("> ");
            out.flush();
            String line = in.readLine();
            if (line == null || "/quit".equalsIgnoreCase(line.trim())) {
                out.println("再见。");
                return;
            }
            if (line.isBlank()) {
                continue;
            }
            String reply = agentService.process(session, line);
            out.println(reply);
        }
    }
}
