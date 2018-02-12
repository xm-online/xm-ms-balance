package com.icthh.xm.ms.balance.service.impl;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.service.MetricService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Metric.
 */
@Service
@Transactional
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;
    private final PermittedRepository permittedRepository;

    public MetricServiceImpl(
                    MetricRepository metricRepository,
                    PermittedRepository permittedRepository) {
        this.metricRepository = metricRepository;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a metric.
     *
     * @param metric the entity to save
     * @return the persisted entity
     */
    @Override
    public Metric save(Metric metric) {
        return metricRepository.save(metric);
    }

    /**
     *  Get all the metrics.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Metric> findAll(String privilegeKey) {
        return permittedRepository.findAll(Metric.class, privilegeKey);
    }

    /**
     *  Get one metric by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Metric findOne(Long id) {
        return metricRepository.findOne(id);
    }

    /**
     *  Delete the  metric by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        metricRepository.delete(id);
    }
}
