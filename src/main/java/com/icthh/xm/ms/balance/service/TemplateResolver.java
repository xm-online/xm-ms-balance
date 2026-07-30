package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        String templateName = method.getParameter("templateName", String.class);
        return List.of(templateName);
    }
}
