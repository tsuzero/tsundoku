package com.tsuzero.tsundoku.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    Optional<SyncLog> findTopBySyncTypeOrderByStartedAtDesc(String syncType);

    @Query("""
            SELECT s FROM SyncLog s
            WHERE s.id IN (
                SELECT MAX(s2.id) FROM SyncLog s2 GROUP BY s2.syncType
            )
            """)
    List<SyncLog> findLatestPerType();
}
