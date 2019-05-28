package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity BalanceChangeEvent and its DTO BalanceChangeEventDto.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BalanceChangeEventMapper extends EntityMapper<BalanceChangeEventDto, BalanceChangeEvent> {
    @Override
    @Mapping(target = "metadata", source = "metadata.metadata" )
    BalanceChangeEventDto toDto(BalanceChangeEvent entity);

    @Override
    @Mapping(target = "metadata.metadata", source = "metadata" )
    BalanceChangeEvent toEntity(BalanceChangeEventDto dto);
}
