package com.icthh.xm.ms.balance.repository;

import static java.time.Instant.now;
import static javax.persistence.LockModeType.WRITE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.NullHandling.NULLS_FIRST;
import static org.springframework.data.domain.Sort.NullHandling.NULLS_LAST;
import static org.springframework.data.jpa.domain.Specifications.where;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Balance_;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.Pocket_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;


/**
 * Spring Data JPA repository for the Pocket entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PocketRepository extends JpaRepository<Pocket, Long>, JpaSpecificationExecutor<Pocket>, ResourceRepository  {
    Optional<Pocket> findByLabelAndStartDateTimeAndEndDateTimeAndBalance(String label, @Nullable Instant startDateTime,
                                                                         @Nullable Instant endDateTime, Balance balance);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pocket as p WHERE p.balance = :balance" +
        " AND ((p.startDateTime < CURRENT_TIMESTAMP()) OR (p.startDateTime IS NULL))" +
        " AND ((p.endDateTime > CURRENT_TIMESTAMP()) OR (p.endDateTime IS NULL))" +
        " ORDER BY p.endDateTime ASC NULLS LAST, p.startDateTime ASC NULLS LAST")
    Page<Pocket> findPocketForCheckoutOrderByDates(@Param("balance") Balance balance, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Pocket e WHERE e.id = :id")
    Optional<Pocket> findOneByIdForUpdate(@Param("id") Long id);
}

