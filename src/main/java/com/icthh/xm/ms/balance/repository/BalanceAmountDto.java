package com.icthh.xm.ms.balance.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceAmountDto {
    private Long id;
    private BigDecimal amount;
}
