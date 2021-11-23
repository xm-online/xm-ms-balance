package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.dto.PocketChangeEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PocketChangeEventMapper extends EntityMapper<PocketChangeEventDto, PocketChangeEvent> {

    @Override
    @Mapping(target = "metadata", source = "metadata.metadata")
    PocketChangeEventDto toDto(PocketChangeEvent entity);

    @Override
    @Mapping(target = "metadata.metadata", source = "metadata" )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    PocketChangeEvent toEntity(PocketChangeEventDto dto);
}
