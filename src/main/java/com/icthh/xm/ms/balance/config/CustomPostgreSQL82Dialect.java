package com.icthh.xm.ms.balance.config;

import io.github.jhipster.domain.util.FixedPostgreSQL82Dialect;
import java.sql.Types;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

public class CustomPostgreSQL82Dialect extends FixedPostgreSQL82Dialect {

    public CustomPostgreSQL82Dialect() {
        super();
        registerFunction("json_field", new SQLFunctionTemplate(StringType.INSTANCE, "?1 ->> ?2"));
    }

}
