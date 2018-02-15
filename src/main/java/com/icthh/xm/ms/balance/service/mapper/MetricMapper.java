package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.*;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Metric and its DTO MetricDTO.
 */
@Mapper(componentModel = "spring", uses = {BalanceMapper.class})
public interface MetricMapper extends EntityMapper<MetricDTO, Metric> {

    @Mapping(source = "balance.id", target = "balanceId")
    MetricDTO toDto(Metric metric);

    @Mapping(source = "balanceId", target = "balance")
    Metric toEntity(MetricDTO metricDTO);

    default Metric fromId(Long id) {
        if (id == null) {
            return null;
        }
        Metric metric = new Metric();
        metric.setId(id);
        return metric;
    }
}
