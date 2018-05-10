package com.icthh.xm.ms.balance.web.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChargingBalanceRequest {
    @NotNull
    private Long balanceId;
    @NotNull
    private BigDecimal amount;
}
