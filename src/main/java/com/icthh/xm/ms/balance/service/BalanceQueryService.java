package com.icthh.xm.ms.balance.service;


import java.util.List;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.service.FilterConverter.QueryPart;
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
        List<Balance> result = findWithPermission(Balance.class, criteria, null, privilegeKey).getContent();
        return balanceMapper.toDto(result);
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
        Page<Balance> result = findWithPermission(Balance.class, criteria, page, privilegeKey);
        return result.map(balanceMapper::toDto);
    }

    /**
     * Find entities with applied filtering and dynamic permissions written in SpEL (rresource condition).
     * @param type Entity class
     * @param criteria Filtering criteria from request
     * @param page page
     * @param privilegeKey privilege key
     * @param <T> Entity type
     * @return Entity page.
     */
    private <T> Page<T> findWithPermission(final Class<T> type,
                                           final BalanceCriteria criteria,
                                           final Pageable page,
                                           final String privilegeKey) {
        QueryPart queryPart = FilterConverter.toJpql(criteria);

        Page<T> result;
        if (queryPart.isEmpty()) {
            result = permittedRepository.findAll(page, type, privilegeKey);
        } else {
            log.debug("find with condition: {}", queryPart);
            result = permittedRepository.findByCondition(queryPart.getQuery().toString(),
                                                         queryPart.getParams(),
                                                         page,
                                                         type,
                                                         privilegeKey);
        }
        return result;
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
