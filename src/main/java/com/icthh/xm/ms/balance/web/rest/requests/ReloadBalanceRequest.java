package com.icthh.xm.ms.balance.web.rest.requests;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class ReloadBalanceRequest {
    @NotNull
    private Long balanceId;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String label;
    private Instant startDateTime;
    private Instant endDateTime;
}
