package com.icthh.xm.ms.balance.config.jsonb;


import com.icthh.xm.commons.migration.db.jsonb.CustomPostgreSQLDialect;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

public class CustomPostgreSQL82Dialect extends CustomPostgreSQLDialect {

    private static final String TEXT_FIELD = "?1 ->> ?2";
    private static final String INT_FIELD = "(?1 ->> ?2)::int";

    public static final String JSON_FIELD_INT = "json_field_int";
    public static final String JSON_FIELD_TEXT = "json_field_text";

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();
        BasicType<String> stringType = typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING);
        BasicType<Integer> intType = typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER);
        functionContributions.getFunctionRegistry().registerPattern(JSON_FIELD_TEXT, TEXT_FIELD, stringType);
        functionContributions.getFunctionRegistry().registerPattern(JSON_FIELD_INT, INT_FIELD, intType);
    }

}
