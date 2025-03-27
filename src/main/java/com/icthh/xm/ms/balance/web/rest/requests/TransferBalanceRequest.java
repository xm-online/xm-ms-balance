package com.icthh.xm.ms.balance.web.rest.requests;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class TransferBalanceRequest {
    @NotNull
    private Long sourceBalanceId;
    @NotNull
    private Long targetBalanceId;
    @NotNull
    @Min(0)
    private BigDecimal amount;
    private Map<String, String> metadata;
    private String uuid;
    private Instant applyDate;
    private String executedBy;

}
