package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BalanceFieldKeyResolver extends AppendLepKeyResolver {
    @Override
    protected List<String> getAppendSegments(LepMethod method) {
        String field = method.getParameter("field", String.class);
        return List.of(toDashSeparatedString(field));
    }

    private static String toDashSeparatedString(String value) {
        return StringUtils.join(
            StringUtils.splitByCharacterTypeCamelCase(value), '-'
            )
            .toUpperCase();
    }
}
