package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity Pocket and its DTO PocketDTO.
 */
@Mapper(componentModel = "spring", uses = {BalanceMapper.class, MetadataMapper.class})
public interface PocketMapper extends EntityMapper<PocketDTO, Pocket> {

    @Mapping(source = "balance.id", target = "balanceId")
    @Mapping(source = "metadata.metadata", target = "metadata")
    PocketDTO toDto(Pocket pocket);

    @Mapping(target = "subtractAmount", ignore = true)
    @Mapping(source = "balanceId", target = "balance")
    @Mapping(source = "metadata", target = "metadata")
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
