package com.icthh.xm.ms.balance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.config.client.api.RefreshableConfiguration;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.BalanceSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceSpecService implements RefreshableConfiguration {

    private final Map<String, Map<String, BalanceSpec.BalanceTypeSpec>> balances = new ConcurrentHashMap<>();

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private final ApplicationProperties applicationProperties;

    private final TenantContextHolder tenantContextHolder;

    private final BalanceSpec.BalanceTypeSpec NULL_OBJECT = new BalanceSpec.BalanceTypeSpec();

    private String getTenantKeyValue() {
        return TenantContextUtils.getRequiredTenantKeyValue(tenantContextHolder);
    }

    @Value("${application.specification-path-pattern}")
    private String balanceSpecPathPattern;

    {
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onRefresh(String updatedKey, String config) {

        String specificationPathPattern = applicationProperties.getSpecificationPathPattern();
        try {
            String tenant = matcher.extractUriTemplateVariables(specificationPathPattern, updatedKey).get("tenantName");
            if (org.apache.commons.lang3.StringUtils.isBlank(config)) {
                balances.remove(tenant);
                return;
            }
            BalanceSpec spec = mapper.readValue(config, BalanceSpec.class);
            balances.put(tenant, toTypeSpecsMap(spec));
            log.info("Specification was for tenant {} updated", tenant);
        } catch (Exception e) {
            log.error("Error read specification from path " + updatedKey, e);
        }
    }

    private Map<String, BalanceSpec.BalanceTypeSpec> toTypeSpecsMap(BalanceSpec spec) {
        return spec.getTypes().stream().collect(toMap(BalanceSpec.BalanceTypeSpec::getKey, identity()));
    }

    public Map<String, BalanceSpec.BalanceTypeSpec> getTypeSpecs() {
        String tenantKeyValue = getTenantKeyValue();
        if (!balances.containsKey(tenantKeyValue)) {
            throw new IllegalArgumentException("Tenant configuration not found");
        }
        return balances.get(tenantKeyValue);
    }

    @Override
    public boolean isListeningConfiguration(String updatedKey) {
        String specificationPathPattern = applicationProperties.getSpecificationPathPattern();
        return matcher.match(specificationPathPattern, updatedKey);
    }

    @Override
    public void onInit(String configKey, String configValue) {
        onRefresh(configKey, configValue);
    }

    public BalanceSpec.BalanceTypeSpec getBalanceSpec(String typeKey) {
        return getTypeSpecs().getOrDefault(typeKey, NULL_OBJECT);
    }
}
