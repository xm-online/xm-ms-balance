package com.icthh.xm.ms.balance.service.lep;

import static com.icthh.xm.commons.lep.TenantScriptStorage.CLASSPATH;

import com.icthh.xm.commons.lep.TenantScriptStorage;
import com.icthh.xm.commons.lep.spring.LepSpringConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class TestLepConfiguration extends LepSpringConfiguration {

    protected TestLepConfiguration(ApplicationEventPublisher eventPublisher, ResourceLoader resourceLoader) {
        super("balance", eventPublisher, resourceLoader);
    }

    @Override
    protected TenantScriptStorage getTenantScriptStorageType() {
        return CLASSPATH;
    }
}
