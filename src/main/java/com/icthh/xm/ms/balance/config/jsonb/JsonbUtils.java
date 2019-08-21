package com.icthh.xm.ms.balance.config.jsonb;

import static com.icthh.xm.ms.balance.domain.Metadata_.json;
import static com.icthh.xm.ms.balance.domain.Pocket_.metadata;

import com.icthh.xm.ms.balance.domain.Pocket;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonbUtils {

    public static Expression<Integer> jsonIntField(CriteriaBuilder cb, Path<Map<String, String>> path, String fieldName) {
        return cb.function("json_field_int", Integer.class, path, cb.literal(fieldName));
    }

    public Expression<Boolean> jsonBooleanField(CriteriaBuilder cb, Path<Map<String, String>> path, String fieldName) {
        return cb.function("json_field_boolean", Boolean.class, path, cb.literal(fieldName));
    }

    public Expression<String> jsonTestField(CriteriaBuilder cb, Path<Map<String, String>> path, String fieldName) {
        return cb.function("json_field_text", String.class, path, cb.literal(fieldName));
    }

}
