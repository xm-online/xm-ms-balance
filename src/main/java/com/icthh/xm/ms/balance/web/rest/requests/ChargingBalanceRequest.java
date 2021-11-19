package com.icthh.xm.ms.balance.web.rest.requests;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChargingBalanceRequest {
    @NotNull
    private Long balanceId;
    @NotNull
    @Min(0)
    private BigDecimal amount;
    private Map<String, String> metadata;
    private String uuid;
    private Instant applyDate;

    public ChargingBalanceRequest(@NotNull Long balanceId, @NotNull @Min(0) BigDecimal amount) {
        this.balanceId = balanceId;
        this.amount = amount;
    }
}
