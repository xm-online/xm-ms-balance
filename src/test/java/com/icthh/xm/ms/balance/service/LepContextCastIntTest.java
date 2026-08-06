package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.lep.XmLepScriptConfigServerResourceLoader;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.LepContext;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_AUTH_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, BalanceApp.class})
@Transactional
public class LepContextCastIntTest {

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private XmAuthenticationContextHolder authContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmLepScriptConfigServerResourceLoader leps;

    @Autowired
    private BalanceHistoryService balanceHistoryService;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @SneakyThrows
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });
    }

    @AfterEach
    public void tearDown() {
        lepManager.endThreadContext();
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
    }

    @Test
    @Transactional
    @SneakyThrows
    public void testLepContextCast() {
        String prefix = "/config/tenants/RESINTTEST/balance/lep/service/";
        String key = prefix + "GetBalanceChangesByTypeAndDate$$around.groovy";
        String body = "import org.springframework.data.domain.PageImpl;\n" +
                "import com.icthh.xm.ms.balance.config.LepContext;\nLepContext context = lepContext\n" +
                "return new PageImpl([['context':context]])";
        leps.onRefresh(key, body);
        Page<?> result = balanceHistoryService.getBalanceChangesByTypeAndDate(null, null);
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.getContent();
        assertTrue(content.get(0).get("context") instanceof LepContext);
        leps.onRefresh(key, null);
    }

}
