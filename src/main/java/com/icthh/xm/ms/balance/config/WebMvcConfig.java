package com.icthh.xm.ms.balance.config;

import com.icthh.xm.commons.lep.spring.web.LepInterceptor;
import com.icthh.xm.commons.web.spring.TenantInterceptor;
import com.icthh.xm.commons.web.spring.TenantVerifyInterceptor;
import com.icthh.xm.commons.web.spring.XmLoggingInterceptor;
import com.icthh.xm.commons.web.spring.config.XmMsWebConfiguration;
import com.icthh.xm.commons.web.spring.config.XmWebMvcConfigurerAdapter;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

@Configuration
@Import({
    XmMsWebConfiguration.class
})
public class WebMvcConfig extends XmWebMvcConfigurerAdapter {

    private final ApplicationProperties applicationProperties;
    private final TenantVerifyInterceptor tenantVerifyInterceptor;
    private final LepInterceptor lepInterceptor;

    public WebMvcConfig(
        TenantInterceptor tenantInterceptor,
        XmLoggingInterceptor xmLoggingInterceptor,
        ApplicationProperties applicationProperties,
        TenantVerifyInterceptor tenantVerifyInterceptor,
        LepInterceptor lepInterceptor) {
        super(tenantInterceptor, xmLoggingInterceptor);
        this.applicationProperties = applicationProperties;
        this.tenantVerifyInterceptor = tenantVerifyInterceptor;
        this.lepInterceptor = lepInterceptor;
    }

    @Override
    protected void xmAddInterceptors(InterceptorRegistry registry) {
        registerTenantInterceptorWithIgnorePathPattern(registry, tenantVerifyInterceptor);
        registerTenantInterceptorWithIgnorePathPattern(registry, lepInterceptor);
    }

    @Override
    protected void xmConfigurePathMatch(PathMatchConfigurer configurer) {
        // no custom configuration
    }

    @Override
    protected List<String> getTenantIgnorePathPatterns() {
        return applicationProperties.getTenantIgnoredPathList();
    }
}
