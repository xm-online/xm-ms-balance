package com.icthh.xm.ms.balance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Properties specific to JHipster.
 *
 * <p>Properties are configured in the application.yml file.
 */
@Component
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Getter
@Setter
public class ApplicationProperties {

    private List<String> tenantIgnoredPathList = Collections.emptyList();
    private boolean timelinesEnabled;
    private boolean kafkaEnabled;
    private String kafkaSystemQueue;

}
