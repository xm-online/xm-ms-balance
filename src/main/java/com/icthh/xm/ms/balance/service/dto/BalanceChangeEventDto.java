package com.icthh.xm.ms.balance.service.dto;

import com.icthh.xm.ms.balance.service.OperationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

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
    private Instant prevOperationDate;
    private BigDecimal amountDelta;
    Map<String, String> metadata;
    private BigDecimal amountBefore;
    private BigDecimal amountAfter;
}
