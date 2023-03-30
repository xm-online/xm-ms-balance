package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class BalanceFieldKeyResolver extends AppendLepKeyResolver {
    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey,
                                         LepMethod method,
                                         LepManagerService managerService) {
        String field = getParamValue(method, "field", String.class);
        String translatedBalanceTypeKey = translateToLepConvention(toDashSeparatedString(field));
        return new String[]{
            translatedBalanceTypeKey
        };
    }

    private static String toDashSeparatedString(String value) {
        return StringUtils.join(
            StringUtils.splitByCharacterTypeCamelCase(value), '-'
            )
            .toUpperCase();
    }
}
