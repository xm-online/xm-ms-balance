package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.domain.Metric;
import java.util.List;

/**
 * Service Interface for managing Metric.
 */
public interface MetricService {

    /**
     * Save a metric.
     *
     * @param metric the entity to save
     * @return the persisted entity
     */
    Metric save(Metric metric);

    /**
     *  Get all the metrics.
     *
     *  @return the list of entities
     */
    List<Metric> findAll(String privilegeKey);

    /**
     *  Get the "id" metric.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Metric findOne(Long id);

    /**
     *  Delete the "id" metric.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
