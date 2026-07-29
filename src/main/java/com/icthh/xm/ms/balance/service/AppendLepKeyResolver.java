package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;

import java.util.List;

public abstract class AppendLepKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        return getAppendSegments(method);
    }

    protected abstract List<String> getAppendSegments(LepMethod method);

}
