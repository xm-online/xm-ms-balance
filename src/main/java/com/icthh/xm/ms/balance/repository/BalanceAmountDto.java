package com.icthh.xm.ms.balance.repository;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceAmountDto {
    private Long id;
    private BigDecimal amount;
}
