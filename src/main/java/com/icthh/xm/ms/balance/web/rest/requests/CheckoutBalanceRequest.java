package com.icthh.xm.ms.balance.web.rest.requests;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class CheckoutBalanceRequest {
    @NotNull
    private Long balanceId;
    @NotNull
    private BigDecimal amount;
}
