package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.lep.groovy.GroovyLepEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * The {@link LepConfiguration} class.
 */
@Configuration
public class LepConfiguration extends GroovyLepEngineConfiguration {

    public LepConfiguration(@Value("${spring.application.name}") String appName) {
        super(appName);
    }

}
