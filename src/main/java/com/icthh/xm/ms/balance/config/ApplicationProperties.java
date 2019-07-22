package com.icthh.xm.ms.balance.config;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Balance.
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Getter
@Setter
public class ApplicationProperties {

    private final Retry retry = new Retry();

    private List<String> tenantIgnoredPathList = Collections.emptyList();
    private boolean timelinesEnabled;
    private boolean kafkaEnabled;
    private String kafkaSystemQueue;
    private Integer pocketChargingBatchSize;
    private String specificationPathPattern;
    private String specificationName;
    private String dbSchemaSuffix;

    @Getter
    @Setter
    private static class Retry {

        private int maxAttempts;
        private long delay;
        private int multiplier;
    }

    private LepProperties lep;

    @Data
    public static class LepProperties {
        private String tenantScriptStorage;
    }

}
