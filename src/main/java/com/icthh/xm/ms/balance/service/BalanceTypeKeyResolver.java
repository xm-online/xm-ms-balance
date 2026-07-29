package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.balance.domain.Balance;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BalanceTypeKeyResolver extends AppendLepKeyResolver {

    @Override
    protected List<String> getAppendSegments(LepMethod method) {
        Balance balance = method.getParameter("balance", Balance.class);
        return List.of(balance.getTypeKey());
    }
}
