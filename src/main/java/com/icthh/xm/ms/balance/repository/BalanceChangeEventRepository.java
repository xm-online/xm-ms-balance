package com.icthh.xm.ms.balance.repository;

import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.balanceEntityId;
import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.operationDate;
import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.operationType;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.service.OperationType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BalanceChangeEventRepository
    extends JpaRepository<BalanceChangeEvent, Long>, JpaSpecificationExecutor<BalanceChangeEvent>, ResourceRepository {

    default Page<BalanceChangeEvent> findByEntityIdInAndOperationTypeAndOperationDateBetween(List<Long> entityIds,
                                                                                             OperationType type,
                                                                                             Instant startDate,
                                                                                             Instant endDate,
                                                                                             Pageable pageable) {

        return findAll(Specification.where((root, cq, cb) -> {

            Predicate entityIdPredicate = cb.disjunction();
            for (Long entityId : entityIds) {
                entityIdPredicate = cb.or(entityIdPredicate, cb.equal(root.get(balanceEntityId), entityId));
            }

            return cb.and(
                cb.greaterThanOrEqualTo(root.get(operationDate), startDate),
                cb.lessThanOrEqualTo(root.get(operationDate), endDate),
                cb.equal(root.get(operationType), type),
                entityIdPredicate
            );
        }), pageable);
    }

    Optional<BalanceChangeEvent> findBalanceChangeEventByOperationId(String operationId);
}
