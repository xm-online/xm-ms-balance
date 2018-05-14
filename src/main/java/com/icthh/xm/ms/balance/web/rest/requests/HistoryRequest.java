package com.icthh.xm.ms.balance.web.rest.requests;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
public class HistoryRequest {
    @NotNull
    private OperationType operationType;
    @NotNull
    private Instant startDate;
    @NotNull
    private Instant endDate;
    @NotNull
    @NotEmpty
    private List<Long> entityIds;
}
