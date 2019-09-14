package com.icthh.xm.ms.balance.config.tenant;

import static com.icthh.xm.commons.config.domain.Configuration.of;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.icthh.xm.commons.config.client.repository.TenantConfigRepository;
import com.icthh.xm.commons.tenantendpoint.TenantManager;
import com.icthh.xm.commons.tenantendpoint.provisioner.TenantAbilityCheckerProvisioner;
import com.icthh.xm.commons.tenantendpoint.provisioner.TenantConfigProvisioner;
import com.icthh.xm.commons.tenantendpoint.provisioner.TenantDatabaseProvisioner;
import com.icthh.xm.commons.tenantendpoint.provisioner.TenantListProvisioner;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@org.springframework.context.annotation.Configuration
public class TenantManagerConfiguration {

    @Bean
    public TenantManager tenantManager(TenantAbilityCheckerProvisioner abilityCheckerProvisioner,
                                       TenantDatabaseProvisioner databaseProvisioner,
                                       TenantConfigProvisioner configProvisioner,
                                       TenantListProvisioner tenantListProvisioner) {

        TenantManager manager = TenantManager.builder()
                                             .service(abilityCheckerProvisioner)
                                             .service(tenantListProvisioner)
                                             .service(configProvisioner)
                                             .service(databaseProvisioner)
                                             .build();
        log.info("Configured tenant manager: {}", manager);
        return manager;
    }

    @SneakyThrows
    @Bean
    public TenantConfigProvisioner tenantConfigProvisioner(TenantConfigRepository tenantConfigRepository,
                                                           ApplicationProperties applicationProperties) {
        TenantConfigProvisioner provisioner = TenantConfigProvisioner
            .builder()
            .tenantConfigRepository(tenantConfigRepository)
            .configuration(of().path(applicationProperties.getSpecificationPathPattern())
                               .content(readResource("/config/balancespec.yml"))
                               .build())
            .build();

        log.info("Configured tenant config provisioner: {}", provisioner);
        return provisioner;
    }

    @SneakyThrows
    private String readResource(String location) {
        return IOUtils.toString(new ClassPathResource(location).getInputStream(), UTF_8);
    }

}
