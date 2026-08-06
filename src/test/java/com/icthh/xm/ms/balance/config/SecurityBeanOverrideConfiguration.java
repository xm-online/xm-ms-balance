package com.icthh.xm.ms.balance.config;

import static com.icthh.xm.commons.config.client.config.XmRestTemplateConfiguration.XM_CONFIG_REST_TEMPLATE;
import static org.mockito.Mockito.mock;

import com.icthh.xm.commons.config.client.repository.CommonConfigRepository;
import com.icthh.xm.commons.security.jwt.TokenProvider;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Overrides UAA specific beans, so they do not interfere the testing
 * This configuration must be included in @SpringBootTest in order to take effect.
 */
@Configuration
public class SecurityBeanOverrideConfiguration {

    @Bean
    @Primary
    public TokenProvider tokenProvider() {
        return mock(TokenProvider.class);
    }

    @Bean
    @Primary
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {
        return mock(RestTemplate.class);
    }

    @Bean(XM_CONFIG_REST_TEMPLATE)
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }

    /**
     * Provided because xm-config is disabled in tests, so {@link com.icthh.xm.commons.config.client.config.XmConfigConfiguration}
     * does not create this bean, while xm-commons-permission requires it unconditionally.
     */
    @Bean
    @Primary
    public CommonConfigRepository commonConfigRepository() {
        return mock(CommonConfigRepository.class);
    }
}
