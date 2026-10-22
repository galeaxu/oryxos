package com.oryxos.boot;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 显式扫描各能力模块的 Spring 组件（Controller 等）。
 *
 * <p>各能力模块的包名不在 com.oryxos.boot 之下，需显式声明。
 */
@Configuration
@ComponentScan(basePackages = {
        "com.oryxos.core",
        "com.oryxos.provider",
        "com.oryxos.memory",
        "com.oryxos.tool",
        "com.oryxos.web",
        "com.oryxos.channel",
        "com.oryxos.storage"
})
public class OryxOsComponentScan {
}
