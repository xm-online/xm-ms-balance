package com.icthh.xm.ms.balance.config.jsonb;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.junit.jupiter.api.Test;

public class NonPostgresJsonAsVarcharContributorUnitTest {

    @Test
    void oracleStoresJsonAsVarchar() {
        JdbcType type = resolveJsonType(OracleDialect.class);

        assertThat(type).isInstanceOf(NonPostgresJsonAsVarcharContributor.JsonAsVarcharJdbcType.class);
    }

    @Test
    void postgresKeepsNativeJson() {
        JdbcType type = resolveJsonType(PostgreSQLDialect.class);

        assertThat(type).isNotInstanceOf(NonPostgresJsonAsVarcharContributor.JsonAsVarcharJdbcType.class);
    }

    private JdbcType resolveJsonType(Class<?> dialect) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", dialect.getName())
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
            .build();
        try (var sessionFactory = (SessionFactoryImplementor) new MetadataSources(registry).buildMetadata().buildSessionFactory()) {
            return sessionFactory.getTypeConfiguration().getJdbcTypeRegistry().getDescriptor(SqlTypes.JSON);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
