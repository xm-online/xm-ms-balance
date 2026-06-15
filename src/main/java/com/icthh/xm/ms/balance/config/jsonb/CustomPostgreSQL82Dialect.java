package com.icthh.xm.ms.balance.config.jsonb;


import com.icthh.xm.commons.migration.db.jsonb.CustomPostgreSQLDialect;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

public class CustomPostgreSQL82Dialect extends CustomPostgreSQLDialect {

    private static final String TEXT_FIELD = "?1 ->> ?2";
    private static final String INT_FIELD = "(?1 ->> ?2)::int";

    public static final String JSON_FIELD_INT = "json_field_int";
    public static final String JSON_FIELD_TEXT = "json_field_text";

    public CustomPostgreSQL82Dialect() {
        super();
        registerFunction(JSON_FIELD_TEXT, new SQLFunctionTemplate(StringType.INSTANCE, TEXT_FIELD));
        registerFunction(JSON_FIELD_INT, new SQLFunctionTemplate(StringType.INSTANCE, INT_FIELD));
    }


//    @Override
//    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
//        BasicType<String> stringBasicType = functionContributions
//                .getTypeConfiguration()
//                .getBasicTypeRegistry()
//                .resolve(StandardBasicTypes.STRING);
//
////        functionContributions.getFunctionRegistry().register(JSON_FIELD_TEXT)
////        functionContributions.getFunctionRegistry().register(JSON_FIELD_INT)
//
//        super.initializeFunctionRegistry(functionContributions);
//    }

}
