package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity Balance and its DTO BalanceDTO.
 */
@Mapper(componentModel = "spring")
public interface BalanceMapper extends EntityMapper<BalanceDTO, Balance> {

    @Mapping(target = "removePockets", ignore = true)
    @Mapping(target = "removeMetrics", ignore = true)
    @Mapping(target = "pockets", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    Balance toEntity(BalanceDTO balanceDTO);

    BalanceDTO toDto(Balance balance);

    default Balance fromId(Long id) {
        if (id == null) {
            return null;
        }
        Balance balance = new Balance();
        balance.setId(id);
        return balance;
    }
}
