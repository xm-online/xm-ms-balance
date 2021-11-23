package com.icthh.xm.ms.balance.domain;

import static com.icthh.xm.ms.balance.service.BalanceService.NEGATIVE_POCKET_LABEL;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BalanceSpec {

    private List<BalanceTypeSpec> types;

    @Data
    public static class BalanceTypeSpec {
        private String key;
        private Map<String, String> name;
        private boolean isWithPockets;
        private boolean removeZeroPockets = false;
        private boolean allowChargeAsManyAsHave = false;
        private AllowNegative allowNegative = new AllowNegative();
    }

    @Data
    public static class AllowNegative {
        private boolean enabled = false;
        private String label = NEGATIVE_POCKET_LABEL;
    }

}
