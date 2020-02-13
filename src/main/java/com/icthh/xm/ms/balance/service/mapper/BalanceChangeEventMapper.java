package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity BalanceChangeEvent and its DTO BalanceChangeEventDto.
 */
@Mapper(componentModel = "spring")
public interface BalanceChangeEventMapper extends EntityMapper<BalanceChangeEventDto, BalanceChangeEvent> {
    @Override
    @Mapping(target = "metadata", source = "metadata.metadata" )
    BalanceChangeEventDto toDto(BalanceChangeEvent entity);

    @Mapping(target = "metadata.metadata", source = "metadata" )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pocketChangeEvents", ignore = true)
    @Mapping(target = "executedByUserKey", ignore = true)
    @Mapping(target = "balanceTypeKey", ignore = true)
    @Mapping(target = "balanceKey", ignore = true)
    @Mapping(target = "balanceEntityId", ignore = true)
    @Override
    BalanceChangeEvent toEntity(BalanceChangeEventDto dto);
}
