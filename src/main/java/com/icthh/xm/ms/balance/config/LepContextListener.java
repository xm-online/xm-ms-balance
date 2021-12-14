package com.icthh.xm.ms.balance.config;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LepContextListener extends SpringLepProcessingApplicationListener {

    private static final String COMMONS = "commons";
    private static final String SERVICES = "services";
    private static final String BALANCE_SERVICE = "balanceService";
    private static final String POCKET_SERVICE = "pocketService";
    private static final String BALANCE_HISTORY_SERVICE = "balanceHistoryService";
    private static final String METRIC_SERVICE = "metricService";

    private final CommonsService commonsService;
    private final BalanceService balanceService;
    private final PocketService pocketService;
    private final BalanceHistoryService balanceHistoryService;
    private final MetricService metricService;

    @Override
    protected void bindExecutionContext(ScopedContext executionContext) {
        Map<String, Object> services = new HashMap<>();
        services.put(BALANCE_SERVICE, balanceService);
        services.put(POCKET_SERVICE, pocketService);
        services.put(BALANCE_HISTORY_SERVICE, balanceHistoryService);
        services.put(METRIC_SERVICE, metricService);

        executionContext.setValue(COMMONS, new CommonsExecutor(commonsService));
        executionContext.setValue(SERVICES, services);
    }
}
