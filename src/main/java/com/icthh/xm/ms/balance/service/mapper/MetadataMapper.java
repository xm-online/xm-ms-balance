package com.icthh.xm.ms.balance.service.mapper;

import com.icthh.xm.ms.balance.domain.Metadata;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetadataMapper extends EntityMapper<Map<String, String>, Metadata> {

    @Override
    default Metadata toEntity(Map<String, String> dto) {
        return new Metadata(dto);
    }

    @Override
    default Map<String, String> toDto(Metadata entity) {
        return entity.getMetadata();
    }

}
