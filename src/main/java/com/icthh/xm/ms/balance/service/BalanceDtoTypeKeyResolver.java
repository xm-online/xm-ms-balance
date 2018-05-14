package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import org.springframework.stereotype.Component;

@Component
public class BalanceDtoTypeKeyResolver extends AppendLepKeyResolver {
    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey, LepMethod method, LepManagerService managerService) {
        BalanceDTO balanceDto = getParamValue(method, "balanceDTO", BalanceDTO.class);
        String translatedBalanceTypeKey = translateToLepConvention(balanceDto.getTypeKey());
        return new String[] {
            translatedBalanceTypeKey
        };
    }
}
