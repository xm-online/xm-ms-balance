package com.icthh.xm.ms.balance.repository;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toMap;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Balance entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long>, JpaSpecificationExecutor<Balance>, ResourceRepository {

    @Query("SELECT SUM(p.amount) FROM Pocket as p WHERE p.balance = :balance AND ((p.startDateTime < :today) OR (p.startDateTime IS NULL)) AND ((p.endDateTime > :today) OR (p.endDateTime IS NULL))")
    Optional<BigDecimal> getBalanceAmount(@Param("balance") Balance balance, @Param("today") Instant today);

    @Query("SELECT new com.icthh.xm.ms.balance.repository.BalanceAmountDto(p.balance.id, SUM(p.amount)) FROM Pocket as p WHERE p.balance in :balances AND ((p.startDateTime < :today) OR (p.startDateTime IS NULL)) AND ((p.endDateTime > :today) OR (p.endDateTime IS NULL)) GROUP BY p.balance")
    List<BalanceAmountDto> getBalancesAmount(@Param("balances") List<Balance> balances, @Param("today") Instant today);

    default Map<Long, BigDecimal> getBalancesAmount(List<Balance> balances) {
        return getBalancesAmount(balances, now()).stream().collect(toMap(BalanceAmountDto::getId, BalanceAmountDto::getAmount));
    }

}
