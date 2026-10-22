package com.oryxos.storage.repository;

import com.oryxos.storage.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * sessions 表的数据访问接口。
 */
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
}
