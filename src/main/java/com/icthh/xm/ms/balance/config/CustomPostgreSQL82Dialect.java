package com.icthh.xm.ms.balance.config;

import io.github.jhipster.domain.util.FixedPostgreSQL82Dialect;
import java.sql.Types;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

public class CustomPostgreSQL82Dialect extends FixedPostgreSQL82Dialect {

    public CustomPostgreSQL82Dialect() {
        super();
        //registerColumnType(Types.JAVA_OBJECT, "jsonb");
        registerFunction("jsonField", new SQLFunctionTemplate(StringType.INSTANCE, "?1 ->> ?2"));
        registerFunction("toJsonb", new SQLFunctionTemplate(StringType.INSTANCE, "to_jsonb(?1)"));
    }

}
