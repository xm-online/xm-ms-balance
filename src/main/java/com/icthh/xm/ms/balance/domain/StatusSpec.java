package com.icthh.xm.ms.balance.domain;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class StatusSpec {
    private String key;
    private Map<String, String> name;
    private List<NextSpec> next;
}
