package com.icthh.xm.ms.balance.service;


import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.CriteriaPermittedRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import io.github.jhipster.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for executing complex queries for Balance entities in the database.
 * The main input is a {@link BalanceCriteria} which get's converted to {@link Specifications},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BalanceDTO} or a {@link Page} of {@link BalanceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BalanceQueryService extends QueryService<Balance> {

    private final CriteriaPermittedRepository permittedRepository;

    private final BalanceMapper balanceMapper;

    private final BalanceRepository balanceRepository;

    /**
     * Return a {@link List} of {@link BalanceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @FindWithPermission("BALANCE.GET_LIST")
    @Transactional(readOnly = true)
    public List<BalanceDTO> findByCriteria(BalanceCriteria criteria, String privilegeKey) {
        List<Balance> result = permittedRepository.findWithPermission(Balance.class, criteria, null, privilegeKey)
                                                  .getContent();
        return balanceMapper.toDto(result);
    }

    /**
     * Return a {@link Page} of {@link BalanceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param pageable The page, which should be returned.
     * @return the matching entities.
     */
    @FindWithPermission("BALANCE.GET_LIST")
    @Transactional(readOnly = true)
    public Page<BalanceDTO> findByCriteria(BalanceCriteria criteria, Pageable pageable, String privilegeKey) {
        Page<Balance> page = permittedRepository.findWithPermission(Balance.class, criteria, pageable, privilegeKey);
        List<BalanceDTO> dtos = page.map(balanceMapper::toDto).getContent();
        Map<Long, BigDecimal> balancesAmount = balanceRepository.getBalancesAmountMap(page.getContent());
        dtos.forEach(it -> it.setAmount(balancesAmount.getOrDefault(it.getId(), BigDecimal.ZERO)));
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    /**
     * Function to convert BalanceCriteria to a {@link Specifications}
     */
    private Specifications<Balance> createSpecification(BalanceCriteria criteria) {
        Specifications<Balance> specification = Specifications.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Balance_.id));
            }
            if (criteria.getKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getKey(), Balance_.key));
            }
            if (criteria.getTypeKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTypeKey(), Balance_.typeKey));
            }
            if (criteria.getMeasureKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMeasureKey(), Balance_.measureKey));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Balance_.amount));
            }
            if (criteria.getReserved() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getReserved(), Balance_.reserved));
            }
            if (criteria.getEntityId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEntityId(), Balance_.entityId));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), Balance_.createdBy));
            }
        }
        return specification;
    }

}
