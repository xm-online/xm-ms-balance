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
    private String value = "{}";

    public Metadata() {}

    @SneakyThrows
    public Metadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? emptyMap() : unmodifiableMap(metadata);
        this.value = objectMapper.writeValueAsString(this.metadata);
    }

    @SneakyThrows
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            value = value == null ? "{}" : value;
            metadata = objectMapper.readValue(value, Map.class);
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

    public Map<String, String> merge(Map<String, String> additionalMetadata) {
        Map<String, String> metadata = new HashMap<>(this.metadata);
        metadata.putAll(additionalMetadata);
        return metadata;
    }

    public Metadata merge(Metadata additionalMetadata) {
        Map<String, String> metadata = new HashMap<>(this.metadata);
        metadata.putAll(additionalMetadata.metadata);
        return new Metadata(metadata);
    }
}
