package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.api.BaseLepContext;
import com.icthh.xm.commons.logging.trace.TraceService.TraceServiceField;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.PocketQueryService;
import com.icthh.xm.ms.balance.service.PocketService;
import org.springframework.web.client.RestTemplate;

public class LepContext extends BaseLepContext implements TraceServiceField {

    public LepServices services;
    public LepTemplates templates;

    public static class LepServices {
        public BalanceService balanceService;
        public PocketService pocketService;
        public BalanceHistoryService balanceHistoryService;
        public MetricService metricService;
        public TenantConfigService tenantConfigService;
        public PocketQueryService pocketQueryService;
    }

    public static class LepTemplates{
        public RestTemplate rest;
    }
}
