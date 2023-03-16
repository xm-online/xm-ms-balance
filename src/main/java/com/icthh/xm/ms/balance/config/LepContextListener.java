package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.commons.CommonsExecutor;
import com.icthh.xm.commons.lep.commons.CommonsService;
import com.icthh.xm.commons.lep.spring.SpringLepProcessingApplicationListener;
import com.icthh.xm.lep.api.ScopedContext;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.PocketService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LepContextListener extends SpringLepProcessingApplicationListener {

    private static final String COMMONS = "commons";
    private static final String SERVICES = "services";
    private static final String BALANCE_SERVICE = "balanceService";
    private static final String POCKET_SERVICE = "pocketService";
    private static final String BALANCE_HISTORY_SERVICE = "balanceHistoryService";
    private static final String METRIC_SERVICE = "metricService";
    public static final String TEMPLATES = "templates";
    public static final String REST_TEMPLATE = "rest";
    public static final String TENANT_CONFIG_SERVICE = "tenantConfigService";
    private final CommonsService commonsService;
    private final BalanceService balanceService;
    private final PocketService pocketService;
    private final BalanceHistoryService balanceHistoryService;
    private final MetricService metricService;
    private final RestTemplate restTemplate;
    private final TenantConfigService tenantConfigService;

    public LepContextListener(CommonsService commonsService, BalanceService balanceService, PocketService pocketService,
                              BalanceHistoryService balanceHistoryService, MetricService metricService,
                              @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate, TenantConfigService tenantConfigService) {
        this.commonsService = commonsService;
        this.balanceService = balanceService;
        this.pocketService = pocketService;
        this.balanceHistoryService = balanceHistoryService;
        this.metricService = metricService;
        this.restTemplate = restTemplate;
        this.tenantConfigService = tenantConfigService;
    }

    @Override
    protected void bindExecutionContext(ScopedContext executionContext) {
        Map<String, Object> services = new HashMap<>();
        services.put(BALANCE_SERVICE, balanceService);
        services.put(POCKET_SERVICE, pocketService);
        services.put(BALANCE_HISTORY_SERVICE, balanceHistoryService);
        services.put(METRIC_SERVICE, metricService);
        services.put(TENANT_CONFIG_SERVICE, tenantConfigService);

        executionContext.setValue(COMMONS, new CommonsExecutor(commonsService));
        executionContext.setValue(SERVICES, services);

        // templates
        Map<String, Object> templates = new HashMap<>();
        templates.put(REST_TEMPLATE, restTemplate);
        executionContext.setValue(TEMPLATES, templates);
    }
}
