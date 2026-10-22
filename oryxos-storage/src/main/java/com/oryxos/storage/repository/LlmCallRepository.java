package com.oryxos.storage.repository;

import com.oryxos.storage.entity.LlmCallEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * llm_calls 表的数据访问接口。
 */
public interface LlmCallRepository extends JpaRepository<LlmCallEntity, String> {
}
