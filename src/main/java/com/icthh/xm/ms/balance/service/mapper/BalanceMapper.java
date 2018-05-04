package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.*;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Balance and its DTO BalanceDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BalanceMapper extends EntityMapper<BalanceDTO, Balance> {


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
