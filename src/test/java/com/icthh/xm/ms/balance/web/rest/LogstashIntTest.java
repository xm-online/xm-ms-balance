package com.icthh.xm.ms.balance.web.rest;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@link LogstashIntTest} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    BalanceApp.class,
    SecurityBeanOverrideConfiguration.class,
    LogstashIntTest.class
})
@Configuration
@TestPropertySource("classpath:config/application-logstash.properties")
public class LogstashIntTest {

    @Test
    public void testLogstashAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> appender = context.getLogger("ROOT").getAppender("ASYNC_LOGSTASH");
        assertThat(appender).isInstanceOf(AsyncAppender.class);
    }

}
