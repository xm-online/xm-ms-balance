package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.BaseLepContext;
import com.icthh.xm.commons.lep.api.LepContextFactory;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.PocketQueryService;
import com.icthh.xm.ms.balance.service.PocketService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BalanceLepContextFactory implements LepContextFactory {

    private final BalanceService balanceService;
    private final PocketService pocketService;
    private final PocketQueryService pocketQueryService;
    private final BalanceHistoryService balanceHistoryService;
    private final MetricService metricService;
    private final RestTemplate restTemplate;
    private final TenantConfigService tenantConfigService;

    public BalanceLepContextFactory(BalanceService balanceService, PocketService pocketService,
                              PocketQueryService pocketQueryService, BalanceHistoryService balanceHistoryService,
                              MetricService metricService, @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                              TenantConfigService tenantConfigService) {
        this.balanceService = balanceService;
        this.pocketService = pocketService;
        this.pocketQueryService = pocketQueryService;
        this.balanceHistoryService = balanceHistoryService;
        this.metricService = metricService;
        this.restTemplate = restTemplate;
        this.tenantConfigService = tenantConfigService;
    }

    @Override
    public BaseLepContext buildLepContext(LepMethod lepMethod) {
        LepContext lepContext = new LepContext();
        lepContext.services = new LepContext.LepServices();
        lepContext.services.balanceService = balanceService;
        lepContext.services.pocketService = pocketService;
        lepContext.services.pocketQueryService = pocketQueryService;
        lepContext.services.balanceHistoryService = balanceHistoryService;
        lepContext.services.metricService = metricService;
        lepContext.services.tenantConfigService = tenantConfigService;
        lepContext.templates = new LepContext.LepTemplates();
        lepContext.templates.rest = restTemplate;
        return lepContext;
    }
}
