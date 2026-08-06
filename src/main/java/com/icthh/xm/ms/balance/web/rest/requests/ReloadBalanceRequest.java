package com.icthh.xm.ms.balance.web.rest.requests;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReloadBalanceRequest {
    @NotNull
    private Long balanceId;
    @NotNull
    @Min(0)
    private BigDecimal amount;
    @NotNull
    private String label;
    private Instant operationDate;
    private Instant startDateTime;
    private Instant endDateTime;
    private Map<String, String> metadata;
    private String uuid;
    private boolean reloadNegativePocket;
    private String executedBy;

}
