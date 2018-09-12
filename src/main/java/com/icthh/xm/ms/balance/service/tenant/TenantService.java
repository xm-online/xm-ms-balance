package com.icthh.xm.ms.balance.service.tenant;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.icthh.xm.commons.config.client.repository.TenantConfigRepository;
import com.icthh.xm.commons.config.client.repository.TenantListRepository;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.logging.aop.IgnoreLogginAspect;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.config.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
@IgnoreLogginAspect
public class TenantService {

    private final TenantContextHolder tenantContextHolder;
    private final TenantDatabaseService databaseService;
    private final TenantListRepository tenantListRepository;
    private final TenantConfigRepository tenantConfigRepository;
    private final ApplicationProperties applicationProperties;

    /**
     * Create tenant.
     *
     * @param tenant tenant key
     */
    public void createTenant(String tenant) {
        StopWatch stopWatch = StopWatch.createStarted();

        String tenantKey = formatTenantKey(tenant);

        log.info("START - SETUP:CreateTenant: tenantKey: {}", tenantKey);

        checkOnlySuperTenantOperation();

        try {
            tenantListRepository.addTenant(tenantKey);
            String content = IOUtils.toString(new ClassPathResource("/config/balancespec.yml").getInputStream(), UTF_8);
            tenantConfigRepository.createConfig(tenantKey, '/' + applicationProperties.getSpecificationName(), content);
            databaseService.create(tenantKey);
            databaseService.migrate(tenantKey);
            log.info("STOP  - SETUP:CreateTenant: tenantKey: {}, result: OK, time = {} ms",
                     tenantKey,
                     stopWatch.getTime());
        } catch (Exception e) {
            log.error("STOP  - SETUP:CreateTenant: tenantKey: {}, result: FAIL, error: {}, time = {} ms",
                      tenantKey,
                      e.getMessage(),
                      stopWatch.getTime());
            throw e;
        }
    }

    /**
     * Delete tenant.
     *
     * @param tenant tenant key
     */
    public void deleteTenant(String tenant) {
        StopWatch stopWatch = StopWatch.createStarted();

        String tenantKey = formatTenantKey(tenant);

        log.info("START - SETUP:DeleteTenant: tenantKey: {}", tenantKey);

        try {
            databaseService.drop(tenantKey);
            tenantListRepository.deleteTenant(tenantKey);

            log.info("STOP  - SETUP:DeleteTenant: tenantKey: {}, result: OK, time = {} ms",
                     tenantKey,
                     stopWatch.getTime());
        } catch (Exception e) {
            log.error("STOP  - SETUP:DeleteTenant: tenantKey: {}, result: FAIL, error: {}, time = {} ms",
                      tenantKey,
                      e.getMessage(),
                      stopWatch.getTime());
            throw e;
        }
    }

    public void manageTenant(String tenant, String state) {
        StopWatch stopWatch = StopWatch.createStarted();

        String tenantKey = formatTenantKey(tenant);

        log.info("START - SETUP:ManageTenant: tenantKey: {}, state: {}", tenantKey, state);

        try {
            tenantListRepository.updateTenant(tenantKey, state.toUpperCase());

            log.info("STOP  - SETUP:ManageTenant: tenantKey: {}, state: {}, result: OK, time = {} ms",
                     tenantKey,
                     state,
                     stopWatch.getTime());
        } catch (Exception e) {
            log.error("STOP  - SETUP:ManageTenant: tenantKey: {}, state: {}, result: FAIL, error: {}, time = {} ms",
                      tenantKey,
                      state,
                      e.getMessage(),
                      stopWatch.getTime());
            throw e;
        }
    }

    private void checkOnlySuperTenantOperation() {
        if (!Constants.TENANT_XM.equals(TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder))) {
            throw new BusinessException("Only 'XM' tenant is allowed to create new tenants");
        }
    }

    private String formatTenantKey(String tenant) {
        return StringUtils.upperCase(tenant);
    }

}
