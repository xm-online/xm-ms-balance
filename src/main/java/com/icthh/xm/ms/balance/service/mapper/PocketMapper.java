package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity Pocket and its DTO PocketDTO.
 */
@Mapper(componentModel = "spring", uses = {BalanceMapper.class})
public interface PocketMapper extends EntityMapper<PocketDTO, Pocket> {

    @Mapping(source = "balance.id", target = "balanceId")
    PocketDTO toDto(Pocket pocket);

    @Mapping(source = "balanceId", target = "balance")
    Pocket toEntity(PocketDTO pocketDTO);

    default Pocket fromId(Long id) {
        if (id == null) {
            return null;
        }
        Pocket pocket = new Pocket();
        pocket.setId(id);
        return pocket;
    }
}
