package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.permission.access.XmPermissionEvaluator;
import com.icthh.xm.commons.security.jwt.TokenProvider;
import com.icthh.xm.commons.security.spring.config.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;

@Configuration
public class BalanceSecurityConfiguration extends SecurityConfiguration {

    public BalanceSecurityConfiguration(TokenProvider tokenProvider,
                                        @Value("${jhipster.security.content-security-policy}")
                                        String contentSecurityPolicy) {
        super(tokenProvider, contentSecurityPolicy);
    }

    /**
     * Keeps the URL rules inherited from xm-commons and only re-opens frame options.
     *
     * <p>The parent configures headers before delegating here, and Spring Security reuses the same
     * headers configurer instance, so disabling frame options at this point overrides the parent's
     * {@code deny()}. This preserves the pre-Spring Boot 4 behaviour, where balance sent no
     * {@code X-Frame-Options} header at all.
     */
    @Override
    protected HttpSecurity applyUrlSecurity(HttpSecurity http) {
        super.applyUrlSecurity(http);
        return http.headers(headers -> headers.frameOptions(FrameOptionsConfig::disable));
    }

    @Bean
    @Primary
    static MethodSecurityExpressionHandler expressionHandler(XmPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
