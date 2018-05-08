package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;
import com.icthh.xm.ms.balance.service.mapper.MetricMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Metric.
 */
@Service
@Transactional
public class MetricService {

    private final MetricRepository metricRepository;
    private final PermittedRepository permittedRepository;

    private final MetricMapper metricMapper;

    public MetricService(MetricRepository metricRepository,
                         MetricMapper metricMapper,
                         PermittedRepository permittedRepository) {
        this.metricRepository = metricRepository;
        this.metricMapper = metricMapper;
        this.permittedRepository = permittedRepository;
    }

    /**
     * Save a metric.
     *
     * @param metricDTO the entity to save
     * @return the persisted entity
     */
    public MetricDTO save(MetricDTO metricDTO) {
        Metric metric = metricMapper.toEntity(metricDTO);
        metric = metricRepository.save(metric);
        return metricMapper.toDto(metric);
    }

    /**
     * Get all the metrics.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<MetricDTO> findAll(String privilegeKey) {
        return permittedRepository.findAll(Metric.class, privilegeKey).stream()
            .map(metricMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one metric by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public MetricDTO findOne(Long id) {
        Metric metric = metricRepository.findOne(id);
        return metricMapper.toDto(metric);
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
