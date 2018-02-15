package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Metric.
 */
@Service
@Transactional
public class MetricService {

    private final MetricRepository metricRepository;
    private final PermittedRepository permittedRepository;

    public MetricService(
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
    public Metric save(Metric metric) {
        return metricRepository.save(metric);
    }

    /**
     * Get all the metrics.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Metric> findAll(String privilegeKey) {
        return permittedRepository.findAll(Metric.class, privilegeKey);
    }

    /**
     * Get one metric by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Metric findOne(Long id) {
        return metricRepository.findOne(id);
    }

    /**
     * Delete the metric by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        metricRepository.delete(id);
    }
}
