package com.icthh.xm.ms.balance.service;

import com.icthh.xm.lep.api.LepKeyResolver;
import com.icthh.xm.lep.api.LepMethod;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BalanceDtoTypeKeyResolver implements LepKeyResolver {

    @Override
    public List<String> segments(LepMethod method) {
        return List.of(
            method.getParameter("balanceDTO", BalanceDTO.class).getTypeKey()
        );
    }
}
