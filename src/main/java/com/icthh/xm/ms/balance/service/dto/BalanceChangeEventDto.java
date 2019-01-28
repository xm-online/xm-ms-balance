package com.icthh.xm.ms.balance.service.dto;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A DTO for the Balance change event.
 */
@Data
@Accessors(chain = true)
public class BalanceChangeEventDto {

    private Long balanceId;
    private OperationType operationType;
    private String operationId;
    private Instant operationDate;
    private BigDecimal amountDelta;
}
