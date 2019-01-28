package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity BalanceChangeEvent and its DTO BalanceChangeEventDto.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BalanceChangeEventMapper extends EntityMapper<BalanceChangeEventDto, BalanceChangeEvent> {
}
