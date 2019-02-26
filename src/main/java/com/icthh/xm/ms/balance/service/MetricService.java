package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;
import com.icthh.xm.ms.balance.service.mapper.MetricMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.UUID.randomUUID;

/**
 * Service Implementation for managing Metric.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MetricService {

    private static final String MAX_METRIC_TYPE_KEY = "MAX";

    private final MetricRepository metricRepository;
    private final PermittedRepository permittedRepository;
    private final BalanceRepository balanceRepository;

    private final MetricMapper metricMapper;


    @Transactional
    public void updateMaxMetric(Balance balance) {
        Metric max = metricRepository.findByTypeKeyAndBalance(MAX_METRIC_TYPE_KEY, balance).orElse(new Metric()
            .key(randomUUID().toString()).typeKey(MAX_METRIC_TYPE_KEY).value("0").balance(balance));
        BigDecimal currentBalance = balanceRepository.findBalanceAmount(balance).orElse(ZERO);
        if (currentBalance.compareTo(new BigDecimal(max.getValue())) > 0) {
            max.setValue(currentBalance.toString());
            metricRepository.save(max);
        }
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
