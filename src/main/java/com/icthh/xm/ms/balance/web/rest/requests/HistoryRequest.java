package com.icthh.xm.ms.balance.web.rest.requests;

import com.icthh.xm.ms.balance.service.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    public List<Long> getEntityIds() {
        if (entityIds == null) {
            entityIds = new ArrayList<>();
        }
        return entityIds;
    }

}
