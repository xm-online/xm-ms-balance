package com.icthh.xm.ms.balance.service;

import static java.time.Instant.now;

import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final XmAuthenticationContextHolder authContextHolder;

    public void onChange(Balance balance, OperationType operationType, BigDecimal amountDelta) {
        onChange(balance, operationType, amountDelta, now());
    }

    public void onChange(Balance balance, OperationType operationType, BigDecimal amountDelta, Instant operationDate) {
        BalanceChangeEvent event = new BalanceChangeEvent();
        event.setBalanceId(balance.getId());
        event.setBalanceKey(balance.getKey());
        event.setBalanceTypeKey(balance.getTypeKey());
        event.setExecutedByUserKey(authContextHolder.getContext().getRequiredUserKey());
        event.setOperationType(operationType);
        event.setAmountDelta(amountDelta);
        event.setOperationDate(operationDate);
    }

}
