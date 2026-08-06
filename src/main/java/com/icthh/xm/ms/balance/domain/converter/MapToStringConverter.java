package com.icthh.xm.ms.balance.domain.converter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Converter
public class MapToStringConverter implements AttributeConverter<Map<String, Object>, String> {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> data) {
        try {
            return mapper.writeValueAsString(data != null ? data : new HashMap<>());
        } catch (JacksonException e) {
            log.warn("Error during JSON to String converting", e);
            return "";
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String data) {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        try {
            return mapper.readValue(StringUtils.isNoneBlank(data) ? data : "{}", typeRef);
        } catch (JacksonException e) {
            log.warn("Error during String to JSON converting", e);
            return Collections.emptyMap();
        }
    }

}
