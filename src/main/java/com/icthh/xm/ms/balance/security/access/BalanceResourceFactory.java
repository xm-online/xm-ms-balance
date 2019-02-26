package com.icthh.xm.ms.balance.security.access;

import com.icthh.xm.commons.permission.access.ResourceFactory;
import com.icthh.xm.commons.permission.access.repository.ResourceRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BalanceResourceFactory implements ResourceFactory {

    private Map<String, ResourceRepository> repositories = new HashMap<>();

    private final BalanceRepository balanceRepository;
    private final MetricRepository metricRepository;
    private final PocketRepository pocketRepository;

    @PostConstruct
    public void init() {
        repositories.put("balance", balanceRepository);
        repositories.put("metric", metricRepository);
        repositories.put("pocket", pocketRepository);
    }

    @Override
    public Object getResource(Object resourceId, String objectType) {
        Object result = null;
        ResourceRepository resourceRepository = repositories.get(objectType);
        if (resourceRepository != null) {
            result = resourceRepository.findResourceById(resourceId);
        }
        return result;
    }
}
