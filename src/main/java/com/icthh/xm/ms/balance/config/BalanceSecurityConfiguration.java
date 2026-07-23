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
