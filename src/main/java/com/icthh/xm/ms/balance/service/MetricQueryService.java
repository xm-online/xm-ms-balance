package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.CriteriaPermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance_;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.domain.Metric_;

import com.icthh.xm.ms.balance.service.dto.MetricCriteria;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;
import com.icthh.xm.ms.balance.service.mapper.MetricMapper;
import io.github.jhipster.service.QueryService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for executing complex queries for Metric entities in the database.
 * The main input is a {@link MetricCriteria} which get's converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MetricDTO} or a {@link Page} of {@link MetricDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MetricQueryService extends QueryService<Metric> {

    private final CriteriaPermittedRepository permittedRepository;

    private final MetricMapper metricMapper;

    public MetricQueryService(CriteriaPermittedRepository permittedRepository, MetricMapper metricMapper) {
        this.permittedRepository = permittedRepository;
        this.metricMapper = metricMapper;
    }

    /**
     * Return a {@link List} of {@link MetricDTO} which matches the criteria from the database
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    @FindWithPermission("METRIC.GET_LIST")
    public List<MetricDTO> findByCriteria(MetricCriteria criteria, String privilegeKey) {
        List<Metric> result = permittedRepository.findWithPermission(Metric.class, criteria, null, privilegeKey)
            .getContent();
        return metricMapper.toDto(result);
    }

    /**
     * Return a {@link Page} of {@link MetricDTO} which matches the criteria from the database
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    @FindWithPermission("METRIC.GET_LIST")
    public Page<MetricDTO> findByCriteria(MetricCriteria criteria, Pageable page, String privilegeKey) {
        Page<Metric> result = permittedRepository.findWithPermission(Metric.class, criteria, page, privilegeKey);
        return result.map(metricMapper::toDto);
    }

    /**
     * Function to convert MetricCriteria to a {@link Specification}
     */
    private Specification<Metric> createSpecification(MetricCriteria criteria) {
        Specification<Metric> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Metric_.id));
            }
            if (criteria.getKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getKey(), Metric_.key));
            }
            if (criteria.getTypeKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTypeKey(), Metric_.typeKey));
            }
            if (criteria.getValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValue(), Metric_.value));
            }
            if (criteria.getBalanceId() != null) {
                specification = specification.and(
                    buildReferringEntitySpecification(criteria.getBalanceId(), Metric_.balance, Balance_.id));
            }
        }
        return specification;
    }

}
