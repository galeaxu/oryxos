package com.oryxos.storage.repository;

import com.oryxos.storage.entity.ToolInvocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * tool_invocations 表的数据访问接口。
 *
 * <p>核心阶段主要用于写入；查询接口放扩展阶段（审计报表）。
 */
public interface ToolInvocationRepository extends JpaRepository<ToolInvocationEntity, String> {
}
