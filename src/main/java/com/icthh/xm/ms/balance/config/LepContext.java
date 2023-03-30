package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.BaseProceedingLep;
import com.icthh.xm.commons.lep.spring.LepThreadHelper;
import com.icthh.xm.commons.lep.spring.lepservice.LepServiceFactory;
import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.tenant.TenantContext;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.PocketQueryService;
import com.icthh.xm.ms.balance.service.PocketService;
import org.springframework.web.client.RestTemplate;

public class LepContext {

    public Object commons;
    public Object inArgs;
    public BaseProceedingLep lep;
    public LepThreadHelper thread;
    public XmAuthenticationContext authContext;
    public TenantContext tenantContext;
    public Object methodResult;

    public LepServiceFactory lepServices;
    public LepServices services;
    public LepTemplates templates;

    public static class LepServices {
        public Object xmTenantLifeCycle; // do not user this field
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
