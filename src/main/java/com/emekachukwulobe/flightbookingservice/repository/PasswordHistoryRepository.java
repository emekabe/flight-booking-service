package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.PasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    /**
     * Returns the most recent {@code N} password hashes for a user,
     * ordered newest-first. Used for password-reuse validation.
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Deletes history entries for a user that fall outside the retention window.
     * Called after inserting a new entry to prune records beyond {@code keepCount}.
     */
    @Modifying
    @Query("""
        DELETE FROM PasswordHistory ph
        WHERE ph.user.id = :userId
        AND ph.id NOT IN (
            SELECT ph2.id FROM PasswordHistory ph2
            WHERE ph2.user.id = :userId
            ORDER BY ph2.createdAt DESC
            LIMIT :keepCount
        )
        """)
    void deleteOldEntriesBeyondLimit(@Param("userId") UUID userId, @Param("keepCount") int keepCount);
}
