package com.icthh.xm.ms.balance.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class NoEnoughMoneyException extends RuntimeException {
    private final Long balanceId;
    private final BigDecimal currentAmount;
}
