package com.icthh.xm.ms.balance.service;


import java.math.BigDecimal;
import java.util.List;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.*; // for static metamodels
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;

import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;

/**
 * Service for executing complex queries for Balance entities in the database.
 * The main input is a {@link BalanceCriteria} which get's converted to {@link Specifications},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BalanceDTO} or a {@link Page} of {@link BalanceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BalanceQueryService extends QueryService<Balance> {

    private final Logger log = LoggerFactory.getLogger(BalanceQueryService.class);


    private final PermittedRepository permittedRepository;

    private final BalanceMapper balanceMapper;

    public BalanceQueryService(PermittedRepository permittedRepository, BalanceMapper balanceMapper) {
        this.permittedRepository = permittedRepository;
        this.balanceMapper = balanceMapper;
    }

    /**
     * Return a {@link List} of {@link BalanceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @FindWithPermission("BALANCE.GET_LIST")
    @Transactional(readOnly = true)
    public List<BalanceDTO> findByCriteria(BalanceCriteria criteria, String privilegeKey) {
        log.debug("find by criteria : {}", criteria);
        final Specifications<Balance> specification = createSpecification(criteria);
        // TODO - implement additional filtering by secification or criteria
        return balanceMapper.toDto(permittedRepository.findAll(Balance.class, privilegeKey));
    }

    /**
     * Return a {@link Page} of {@link BalanceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @FindWithPermission("BALANCE.GET_LIST")
    @Transactional(readOnly = true)
    public Page<BalanceDTO> findByCriteria(BalanceCriteria criteria, Pageable page, String privilegeKey) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specifications<Balance> specification = createSpecification(criteria);
        // TODO - implement additional filtering by secification or criteria
        final Page<Balance> result = permittedRepository.findAll(page, Balance.class, privilegeKey);
        return result.map(balanceMapper::toDto);
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
            if (criteria.getPocketsId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getPocketsId(), Balance_.pockets, Pocket_.id));
            }
            if (criteria.getMetricsId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getMetricsId(), Balance_.metrics, Metric_.id));
            }
        }
        return specification;
    }

}
