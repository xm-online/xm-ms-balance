package com.icthh.xm.ms.balance.config.jsonb;

import io.github.jhipster.domain.util.FixedPostgreSQL82Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

public class CustomPostgreSQL82Dialect extends FixedPostgreSQL82Dialect {

    private static final String TEXT_FIELD = "?1 ->> ?2";
    private static final String INT_FIELD = "(?1 ->> ?2)::int";

    public static final String JSON_FIELD_INT = "json_field_int";
    public static final String JSON_FIELD_TEXT = "json_field_text";

    public CustomPostgreSQL82Dialect() {
        super();
        registerFunction(JSON_FIELD_TEXT, new SQLFunctionTemplate(StringType.INSTANCE, TEXT_FIELD));
        registerFunction(JSON_FIELD_INT, new SQLFunctionTemplate(StringType.INSTANCE, INT_FIELD));
    }

}
