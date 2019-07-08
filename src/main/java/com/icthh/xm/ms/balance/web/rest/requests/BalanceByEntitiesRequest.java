package com.icthh.xm.ms.balance.web.rest.requests;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BalanceByEntitiesRequest {

    private List<Long> entityIds = new ArrayList<>();

    public List<Long> getEntityIds() {
        if (entityIds == null) {
            entityIds = new ArrayList<>();
        }
        return entityIds;
    }
}
