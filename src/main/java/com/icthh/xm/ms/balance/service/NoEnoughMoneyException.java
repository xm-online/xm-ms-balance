package com.icthh.xm.ms.balance.service;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.valueOf;

import com.icthh.xm.commons.exceptions.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class NoEnoughMoneyException extends BusinessException {
    private final Long balanceId;
    private final BigDecimal currentAmount;

    public NoEnoughMoneyException(Long balanceId, BigDecimal currentAmount) {
        super("error.no.enough.many", "No enough many", of("balanceId", valueOf(balanceId), "currentAmount", valueOf(currentAmount)));
        this.balanceId = balanceId;
        this.currentAmount = currentAmount;
    }
}
