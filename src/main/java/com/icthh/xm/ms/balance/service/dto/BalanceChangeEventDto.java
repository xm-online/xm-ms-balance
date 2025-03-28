package com.icthh.xm.ms.balance.service.dto;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A DTO for the Balance change event.
 */
@Data
@Accessors(chain = true)
public class BalanceChangeEventDto {

    private Long id;
    private Long balanceId;
    private Long balanceEntityId;
    private String balanceKey;
    private String balanceTypeKey;
    private String executedByUserKey;
    private String executedBy;
    private OperationType operationType;
    private String operationId;
    private Instant operationDate;
    private Instant entryDate;
    private Instant prevEntryDate;
    private BigDecimal amountDelta;
    private BigDecimal amountTotal;
    Map<String, String> metadata;
    private BigDecimal amountBefore;
    private BigDecimal amountAfter;
    private Boolean last;
    private String revertOperationId;
    private List<PocketChangeEventDto> pocketChangeEvents;
}
