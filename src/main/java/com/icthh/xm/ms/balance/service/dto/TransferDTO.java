package com.icthh.xm.ms.balance.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferDTO {
    private BalanceChangeEventDto from;
    private BalanceChangeEventDto to;
}
