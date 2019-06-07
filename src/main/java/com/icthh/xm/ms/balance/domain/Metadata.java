package com.icthh.xm.ms.balance.domain;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@Embeddable
@EqualsAndHashCode
public class Metadata implements Serializable {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

    @Transient
    private Map<String, String> metadata = null;

    @Column(name="metadata_value")
    private String value = null;

    public Metadata() {}

    @SneakyThrows
    public Metadata(Map<String, String> metadata) {
        if (metadata != null) {
            this.value = objectMapper.writeValueAsString(metadata);
            this.metadata = unmodifiableMap(metadata);
        }
    }

    @SneakyThrows
    public Map<String, String> getMetadata() {
        if (metadata == null && value != null) {
            metadata = unmodifiableMap(objectMapper.readValue(value, Map.class));
        }
        return metadata;
    }

    public static Metadata of(Map<String, String> metadata) {
        return new Metadata(metadata);
    }

    @Override
    public String toString() {
        return value;
    }

    public Metadata merge(Metadata additionalMetadata) {
        if (this.getMetadata() == null && additionalMetadata.getMetadata() == null) {
            return new Metadata();
        }

        Map<String, String> metadata = new HashMap<>(nullSafe(this.getMetadata()));
        metadata.putAll(nullSafe(additionalMetadata.getMetadata()));
        return new Metadata(metadata);
    }

    public Map<String, String> nullSafe(Map<String, String> map) {
        return map == null ? new HashMap<>() : map;
    }
}
