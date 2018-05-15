package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import com.icthh.xm.ms.balance.domain.Balance;
import org.springframework.stereotype.Component;

@Component
public class BalanceTypeKeyResolver extends AppendLepKeyResolver {
    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey, LepMethod method, LepManagerService managerService) {
        Balance balance = getParamValue(method, "balance", Balance.class);
        String translatedBalanceTypeKey = translateToLepConvention(balance.getTypeKey());
        return new String[] {
            translatedBalanceTypeKey
        };
    }
}
