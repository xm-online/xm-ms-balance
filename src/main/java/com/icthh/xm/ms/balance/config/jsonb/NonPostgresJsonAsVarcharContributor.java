package com.icthh.xm.ms.balance.config.jsonb;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

/**
 * Columns mapped with {@code SqlTypes.JSON} are native jsonb only on Postgres.
 * On other databases (e.g. Oracle, where the column is varchar) Hibernate would pick a binary JSON type,
 * so JSON is stored there as plain varchar text.
 */
@Slf4j
public class NonPostgresJsonAsVarcharContributor implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        Dialect dialect = serviceRegistry.requireService(JdbcServices.class).getDialect();
        if (dialect instanceof PostgreSQLDialect) {
            log.info("Run on {} dialect. JSON columns are kept as jsonb.", dialect);
            return;
        }
        log.info("Run on {} dialect. JSON columns will be stored as varchar.", dialect);
        typeContributions.contributeJdbcType(JsonAsVarcharJdbcType.INSTANCE);
    }

    static class JsonAsVarcharJdbcType extends VarcharJdbcType {

        static final JsonAsVarcharJdbcType INSTANCE = new JsonAsVarcharJdbcType();

        @Override
        public int getDefaultSqlTypeCode() {
            return SqlTypes.JSON;
        }
    }
}
