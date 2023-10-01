package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.balance.domain.Balance;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BalanceTypeKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        return List.of(
            method.getParameter("balance", Balance.class).getTypeKey()
        );
    }
}
