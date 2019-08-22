package com.icthh.xm.ms.balance.config.jsonb;

import static com.icthh.xm.ms.balance.config.jsonb.CustomPostgreSQL82Dialect.JSON_FIELD_INT;
import static com.icthh.xm.ms.balance.config.jsonb.CustomPostgreSQL82Dialect.JSON_FIELD_TEXT;

import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonbUtils {

    public static Expression<Integer> jsonIntField(CriteriaBuilder cb, Path<Map<String, String>> path, String fieldName) {
        return cb.function(JSON_FIELD_INT, Integer.class, path, cb.literal(fieldName));
    }

    public Expression<String> jsonTextField(CriteriaBuilder cb, Path<Map<String, String>> path, String fieldName) {
        return cb.function(JSON_FIELD_TEXT, String.class, path, cb.literal(fieldName));
    }

}
