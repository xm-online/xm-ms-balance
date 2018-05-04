package com.icthh.xm.ms.balance.repository;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;


/**
 * Spring Data JPA repository for the Pocket entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PocketRepository extends JpaRepository<Pocket, Long>, ResourceRepository {
    Optional<Pocket> findByLabelAndStartDateTimeAndEndDateTimeAndBalance(String label, Instant startDateTime, Instant endDateTime, Balance balance);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Pocket e WHERE e.id = :id")
    Optional<Pocket> findOneByIdForUpdate(@Param("id") Long id);
}
