package com.icthh.xm.ms.balance.config;

import com.opentable.db.postgres.embedded.LiquibasePreparer;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.PreparedDbRule;
import javax.sql.DataSource;
import org.junit.Rule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddedPgConfiguration {

    @Rule
    public PreparedDbRule db = EmbeddedPostgresRules.preparedDatabase(
        LiquibasePreparer.forClasspathLocation("liquibase/master.xml"));

    @Bean
    @Primary
    public DataSource dataSource() {
        return db.getTestDatabase();
    }
}
