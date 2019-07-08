package com.icthh.xm.ms.balance.repository;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.jpa.domain.Specification.where;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Balance_;
import com.icthh.xm.ms.balance.web.rest.requests.BalanceByEntitiesRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Balance entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long>, JpaSpecificationExecutor<Balance>, ResourceRepository {

    @Query("SELECT SUM(p.amount) FROM Pocket as p WHERE p.balance = :balance AND ((p.startDateTime < CURRENT_TIMESTAMP()) OR (p.startDateTime IS NULL)) AND ((p.endDateTime > CURRENT_TIMESTAMP()) OR (p.endDateTime IS NULL))")
    Optional<BigDecimal> findBalanceAmount(@Param("balance") Balance balance);

    @Query("SELECT new com.icthh.xm.ms.balance.repository.BalanceAmountDto(p.balance.id, SUM(p.amount)) FROM Pocket as p WHERE p.balance in :balances AND ((p.startDateTime < CURRENT_TIMESTAMP()) OR (p.startDateTime IS NULL)) AND ((p.endDateTime > CURRENT_TIMESTAMP()) OR (p.endDateTime IS NULL)) GROUP BY p.balance")
    List<BalanceAmountDto> getBalancesAmount(@Param("balances") List<Balance> balances);

    default Map<Long, BigDecimal> getBalancesAmountMap(List<Balance> balances) {
        return getBalancesAmount(balances).stream().collect(toMap(BalanceAmountDto::getId, BalanceAmountDto::getAmount));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Balance e WHERE e.id = :id")
    Optional<Balance> findOneByIdForUpdate(@Param("id") Long id);

    default Page<Balance> findByEntityIds(List<Long> entityIds, Pageable pageable) {
        return findAll(where((root, query, builder) -> builder.or(toPredicates(entityIds, root, builder))), pageable);
    }

    default Predicate[] toPredicates(List<Long> entityIds, Root<Balance> root, CriteriaBuilder builder) {
        List<Predicate> predicates = entityIds.stream()
                                              .map(entityId -> toPredicate(root, builder, entityId))
                                              .collect(toList());
        Predicate[] predicatesArrays = new Predicate[predicates.size()];
        predicates.toArray(predicatesArrays);
        return predicatesArrays;
    }

    default Predicate toPredicate(Root<Balance> root, CriteriaBuilder builder, Long entityId) {
        return builder.equal(root.get(Balance_.entityId), entityId);
    }
}
