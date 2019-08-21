package com.icthh.xm.ms.balance.config.jsonb;

import io.github.jhipster.domain.util.FixedPostgreSQL82Dialect;
import java.sql.Types;
import javax.persistence.criteria.CriteriaBuilder;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

public class CustomPostgreSQL82Dialect extends FixedPostgreSQL82Dialect {

    public CustomPostgreSQL82Dialect() {
        super();
        registerFunction("json_field_text", new SQLFunctionTemplate(StringType.INSTANCE, "?1 ->> ?2"));
        registerFunction("json_field_int", new SQLFunctionTemplate(StringType.INSTANCE, "(?1 ->> ?2)::int"));
        registerFunction("json_field_boolean", new SQLFunctionTemplate(StringType.INSTANCE, "(?1 ->> ?2)::boolean"));
    }

}
