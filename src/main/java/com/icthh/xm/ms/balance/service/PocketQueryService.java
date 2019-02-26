package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.CriteriaPermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance_;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.Pocket_;

import com.icthh.xm.ms.balance.service.dto.PocketCriteria;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import com.icthh.xm.ms.balance.service.mapper.PocketMapper;

import io.github.jhipster.service.QueryService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for executing complex queries for Pocket entities in the database.
 * The main input is a {@link PocketCriteria} which get's converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link PocketDTO} or a {@link Page} of {@link PocketDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PocketQueryService extends QueryService<Pocket> {

    private final CriteriaPermittedRepository permittedRepository;

    private final PocketMapper pocketMapper;

    public PocketQueryService(CriteriaPermittedRepository permittedRepository, PocketMapper pocketMapper) {
        this.permittedRepository = permittedRepository;
        this.pocketMapper = pocketMapper;
    }

    /**
     * Return a {@link List} of {@link PocketDTO} which matches the criteria from the database
     *
     * @param criteria     The object which holds all the filters, which the entities should match.
     * @param privilegeKey XM Privilege key
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    @FindWithPermission("POCKET.GET_LIST")
    public List<PocketDTO> findByCriteria(PocketCriteria criteria, String privilegeKey) {
        List<Pocket> result = permittedRepository.findWithPermission(Pocket.class, criteria, null, privilegeKey)
            .getContent();
        return pocketMapper.toDto(result);
    }

    /**
     * Return a {@link Page} of {@link PocketDTO} which matches the criteria from the database
     *
     * @param criteria     The object which holds all the filters, which the entities should match.
     * @param page         The page, which should be returned.
     * @param privilegeKey Xm privilege key
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    @FindWithPermission("POCKET.GET_LIST")
    public Page<PocketDTO> findByCriteria(PocketCriteria criteria, Pageable page, final String privilegeKey) {
        Page<Pocket> result = permittedRepository.findWithPermission(Pocket.class, criteria, page, privilegeKey);
        return result.map(pocketMapper::toDto);
    }

    /**
     * Function to convert PocketCriteria to a {@link Specification}
     */
    private Specification<Pocket> createSpecification(PocketCriteria criteria) {
        Specification<Pocket> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Pocket_.id));
            }
            if (criteria.getKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getKey(), Pocket_.key));
            }
            if (criteria.getLabel() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLabel(), Pocket_.label));
            }
            if (criteria.getStartDateTime() != null) {
                specification = specification.and(
                    buildRangeSpecification(criteria.getStartDateTime(), Pocket_.startDateTime));
            }
            if (criteria.getEndDateTime() != null) {
                specification = specification.and(
                    buildRangeSpecification(criteria.getEndDateTime(), Pocket_.endDateTime));
            }
            if (criteria.getAmount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAmount(), Pocket_.amount));
            }
            if (criteria.getReserved() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getReserved(), Pocket_.reserved));
            }
            if (criteria.getBalanceId() != null) {
                specification = specification.and(
                    buildReferringEntitySpecification(criteria.getBalanceId(), Pocket_.balance, Balance_.id));
            }
        }
        return specification;
    }

}
