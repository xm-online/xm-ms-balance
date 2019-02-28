package com.icthh.xm.ms.balance.repository;

import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.balanceEntityId;
import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.operationDate;
import static com.icthh.xm.ms.balance.domain.BalanceChangeEvent_.operationType;
import static com.icthh.xm.ms.balance.domain.PocketChangeEvent_.transaction;

import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.OperationType;

import java.time.Instant;
import java.util.List;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PocketChangeEventRepository
    extends JpaRepository<PocketChangeEvent, Long>, JpaSpecificationExecutor<PocketChangeEvent>, ResourceRepository {

    default Page<PocketChangeEvent> findByEntityIdInAndOperationTypeAndOperationDateBetween(List<Long> entityIds,
                                                                                            OperationType type,
                                                                                            Instant startDate,
                                                                                            Instant endDate,
                                                                                            Pageable pageable) {

        return findAll(Specification.where((root, cq, cb) -> {

            Predicate entityIdPredicate = cb.disjunction();
            for (Long entityId : entityIds) {
                entityIdPredicate = cb.or(entityIdPredicate,
                    cb.equal(root.get(transaction).get(balanceEntityId), entityId));
            }

            return cb.and(
                cb.greaterThanOrEqualTo(root.get(transaction).get(operationDate), startDate),
                cb.lessThanOrEqualTo(root.get(transaction).get(operationDate), endDate),
                cb.equal(root.get(transaction).get(operationType), type),
                entityIdPredicate
            );
        }), pageable);
    }

}
