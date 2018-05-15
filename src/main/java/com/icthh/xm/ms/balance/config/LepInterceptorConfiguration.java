package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.lep.spring.web.LepInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@RequiredArgsConstructor
@Configuration
public class LepInterceptorConfiguration extends WebMvcConfigurerAdapter {

    private final LepInterceptor lepInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(lepInterceptor).addPathPatterns("/**");
    }

}
