package com.icthh.xm.ms.balance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfiguration {

    @Value("${spring.cloud.loadbalancer.enabled:true}")
    private Boolean loadBalancedTemplateEnabled;

    @Bean
    @Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate(ObjectProvider<RestTemplateCustomizer> customizerProvider) {
        RestTemplate restTemplate = new RestTemplate();
        if (loadBalancedTemplateEnabled) {
            log.info("loadBalancedRestTemplate: using spring load balancer");
            customizerProvider.ifAvailable(customizer -> customizer.customize(restTemplate));
        }
        return restTemplate;
    }

    @Bean
    @Qualifier("vanillaRestTemplate")
    public RestTemplate vanillaRestTemplate() {
        return new RestTemplate();
    }

}
