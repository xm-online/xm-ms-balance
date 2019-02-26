package com.icthh.xm.ms.balance.web.rest.requests;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TemplateParamsHolder {
    private Map<String, Object> templateParams = new HashMap<>();
}
