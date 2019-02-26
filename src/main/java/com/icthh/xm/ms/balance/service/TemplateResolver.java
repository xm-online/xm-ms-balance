package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepManagerService;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.lep.api.commons.SeparatorSegmentedLepKey;
import org.springframework.stereotype.Component;

@Component
public class TemplateResolver extends AppendLepKeyResolver {
    @Override
    protected String[] getAppendSegments(SeparatorSegmentedLepKey baseKey, LepMethod method, LepManagerService managerService) {
        String templateName = getRequiredStrParam(method, "templateName");
        String translatedBalanceTypeKey = translateToLepConvention(templateName);
        return new String[] {
            translatedBalanceTypeKey
        };
    }
}
