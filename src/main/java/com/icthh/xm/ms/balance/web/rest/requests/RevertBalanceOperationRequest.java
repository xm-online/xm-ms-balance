package com.icthh.xm.ms.balance.web.rest.requests;

import com.icthh.xm.ms.balance.service.RevertReloadMode;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

import static com.icthh.xm.ms.balance.service.RevertReloadMode.DEFAULT;

@Data
@Accessors(chain = true)
public class RevertBalanceOperationRequest {

    @NotNull
    private Long balanceId;
    private String uuid;
    @NotNull
    private String uuidToRevert;
    private Map<String, String> metadata;

    private RevertReloadMode revertReloadMode = DEFAULT;

    private Instant startDateTime;
    private Instant endDateTime;
    private String label;
    private String executedBy;

}
