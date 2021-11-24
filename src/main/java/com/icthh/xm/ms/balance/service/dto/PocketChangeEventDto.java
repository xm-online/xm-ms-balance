package com.icthh.xm.ms.balance.service.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PocketChangeEventDto {

    private Long pocketId;
    private String pocketKey;
    private String pocketLabel;
    Map<String, String> metadata;
    private BigDecimal amountDelta;
    private BigDecimal amountBefore;
    private BigDecimal amountAfter;
}
