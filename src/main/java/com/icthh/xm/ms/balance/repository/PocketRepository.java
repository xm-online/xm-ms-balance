package com.icthh.xm.ms.balance.repository;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metadata;
import com.icthh.xm.ms.balance.domain.Pocket;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Pocket entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PocketRepository
    extends JpaRepository<Pocket, Long>, JpaSpecificationExecutor<Pocket>, ResourceRepository {

    default Optional<Pocket> findPocketForReload(String label,
                                         @Nullable Instant startDateTime,
                                         @Nullable Instant endDateTime,
                                         Balance balance,
                                         String metadataValue) {
        return findByLabelAndStartDateTimeAndEndDateTimeAndBalanceAndMetadataValue(label, startDateTime, endDateTime,
                                                                                   balance, metadataValue);
    }

    Optional<Pocket> findByLabelAndStartDateTimeAndEndDateTimeAndBalanceAndMetadataValue(String label,
                                                                                         @Nullable Instant startDateTime,
                                                                                         @Nullable Instant endDateTime,
                                                                                         Balance balance,
                                                                                         String metadataValue);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pocket as p WHERE p.balance = :balance AND p.amount > 0 "
        + " AND ((p.startDateTime < CURRENT_TIMESTAMP()) OR (p.startDateTime IS NULL))"
        + " AND ((p.endDateTime > CURRENT_TIMESTAMP()) OR (p.endDateTime IS NULL))"
        + " ORDER BY p.endDateTime ASC NULLS LAST, p.startDateTime ASC NULLS LAST")
    Page<Pocket> findPocketForChargingOrderByDates(@Param("balance") Balance balance, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pocket as p WHERE p.balance = :balance "
        + " AND ((p.startDateTime < CURRENT_TIMESTAMP()) OR (p.startDateTime IS NULL))"
        + " AND ((p.endDateTime > CURRENT_TIMESTAMP()) OR (p.endDateTime IS NULL))"
        + " ORDER BY p.endDateTime ASC NULLS LAST, p.startDateTime ASC NULLS LAST")
    Page<Pocket> findPocketForChargingWithNegativeOrderByDates(@Param("balance") Balance balance, Pageable pageable);

    Optional<Pocket> findByLabelAndBalanceId(String label, Long balanceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Pocket e WHERE e.id = :id")
    Optional<Pocket> findOneByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Pocket e WHERE e.balance.id = :balanceId AND e.amount = 0")
    void deletePocketWithZeroAmount(@Param("balanceId") Long balanceId);

}

