package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.security.jwt.TokenProvider;
import com.icthh.xm.commons.security.spring.config.SecurityConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
        http.headers(headers -> headers.frameOptions(FrameOptionsConfig::disable));
        return super.applyUrlSecurity(http);
    }
}
