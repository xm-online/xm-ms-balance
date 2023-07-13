package com.icthh.xm.ms.balance.web.rest;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.lep.XmLepScriptConfigServerResourceLoader;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.PocketChangeEventRepository;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepScriptConstants.BINDING_KEY_AUTH_CONTEXT;
import static com.icthh.xm.ms.balance.web.rest.TestUtil.createFormattingConversionService;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the BalanceHistoryResource REST controller.
 *
 * @see BalanceHistoryResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, BalanceApp.class})
public class BalanceHistoryResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final Long DEFAULT_ENTITY_ID = 1L;

    private MockMvc restBalanceHistoryMockMvc;

    @Autowired
    BalanceHistoryService balanceHistoryService;

    @Autowired
    private BalanceChangeEventRepository balanceChangeEventRepository;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmAuthenticationContextHolder xmAuthenticationContextHolder;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @BeforeTransaction
    public void BeforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
        lepManager.beginThreadContext(scopedContext -> {
            scopedContext.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            scopedContext.setValue(BINDING_KEY_AUTH_CONTEXT, xmAuthenticationContextHolder.getContext());
        });
    }

    @After
    public void tearDown() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
        lepManager.endThreadContext();
    }

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        final BalanceHistoryResource balanceHistoryResource = new BalanceHistoryResource(balanceHistoryService);
        this.restBalanceHistoryMockMvc = MockMvcBuilders.standaloneSetup(balanceHistoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @DataSet(value = "mockHistory-init.xml", disableConstraints = true)
    @Transactional
    public void getAllBalance() throws Exception {

        BalanceChangeEvent balanceChangeEvent = new BalanceChangeEvent();
        balanceChangeEvent.setId(DEFAULT_ENTITY_ID);
        balanceChangeEvent.setBalanceEntityId(DEFAULT_ENTITY_ID);
        balanceChangeEvent.setBalanceId(DEFAULT_ENTITY_ID);
        balanceChangeEvent.setBalanceKey(DEFAULT_KEY);
        balanceChangeEvent.setBalanceTypeKey(DEFAULT_TYPE_KEY);
        balanceChangeEvent.setAmountAfter(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountBefore(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountDelta(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountTotal(DEFAULT_AMOUNT);
        balanceChangeEvent.setOperationType(OperationType.RELOAD);
        balanceChangeEvent.setEntryDate(Instant.now());
        balanceChangeEvent.setOperationDate(Instant.now());
        balanceChangeEvent.setOperationId("STUB");
        balanceChangeEvent.setExecutedByUserKey("STUB");
        balanceChangeEvent.setLast(true);


        PocketChangeEvent pocketChangeEvent = PocketChangeEvent.builder()
            .pocketId(DEFAULT_ENTITY_ID)
            .amountAfter(DEFAULT_AMOUNT)
            .amountBefore(DEFAULT_AMOUNT)
            .amountDelta(DEFAULT_AMOUNT)
            .pocketLabel("label")
            .pocketKey(DEFAULT_KEY)
            .build();

        balanceChangeEvent.addPocketChangeEvent(pocketChangeEvent);
        balanceChangeEventRepository.saveAndFlush(balanceChangeEvent);

        restBalanceHistoryMockMvc.perform(get("/api/v2/balances/history?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].balanceKey").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].balanceTypeKey").value(hasItem(DEFAULT_TYPE_KEY)))
            .andExpect(jsonPath("$.[*].balanceId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].operationType").value(hasItem(OperationType.RELOAD.toString())))
            .andExpect(jsonPath("$.[*].amountAfter").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].amountTotal").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andDo(print());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByKeyIsEqualToSomething() throws Exception {
        BalanceChangeEvent balanceChangeEvent = new BalanceChangeEvent();
        balanceChangeEvent.setBalanceEntityId(DEFAULT_ENTITY_ID);
        balanceChangeEvent.setBalanceId(DEFAULT_ENTITY_ID);
        balanceChangeEvent.setBalanceKey(DEFAULT_KEY);
        balanceChangeEvent.setBalanceTypeKey(DEFAULT_TYPE_KEY);
        balanceChangeEvent.setAmountAfter(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountBefore(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountDelta(DEFAULT_AMOUNT);
        balanceChangeEvent.setAmountTotal(DEFAULT_AMOUNT);
        balanceChangeEvent.setOperationType(OperationType.RELOAD);
        balanceChangeEvent.setEntryDate(Instant.now());
        balanceChangeEvent.setOperationDate(Instant.now());
        balanceChangeEvent.setOperationId("STUB");
        balanceChangeEvent.setExecutedByUserKey("STUB");
        balanceChangeEvent.setLast(true);

        balanceChangeEventRepository.saveAndFlush(balanceChangeEvent);

        restBalanceHistoryMockMvc.perform(get("/api/v2/balances/history?sort=id,desc&operationType.in=RELOAD"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].balanceKey").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].balanceTypeKey").value(hasItem(DEFAULT_TYPE_KEY)))
            .andExpect(jsonPath("$.[*].balanceId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].operationType").value(hasItem(OperationType.RELOAD.toString())))
            .andExpect(jsonPath("$.[*].amountAfter").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].amountTotal").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andDo(print());

        restBalanceHistoryMockMvc.perform(get("/api/v2/balances/history?sort=id,desc&balanceTypeKey.equals=AAAAAAAAAA"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].balanceKey").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].balanceTypeKey").value(hasItem(DEFAULT_TYPE_KEY)))
            .andExpect(jsonPath("$.[*].balanceId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].operationType").value(hasItem(OperationType.RELOAD.toString())))
            .andExpect(jsonPath("$.[*].amountAfter").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].amountTotal").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andDo(print());

        restBalanceHistoryMockMvc.perform(get("/api/v2/balances/history?sort=id,desc&amountAfter.greatThat=0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].balanceKey").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].balanceTypeKey").value(hasItem(DEFAULT_TYPE_KEY)))
            .andExpect(jsonPath("$.[*].balanceId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].operationType").value(hasItem(OperationType.RELOAD.toString())))
            .andExpect(jsonPath("$.[*].amountAfter").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].amountTotal").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andDo(print());

        restBalanceHistoryMockMvc.perform(get("/api/v2/balances/history?sort=id,desc&oprationId.contains=tu"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].balanceKey").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].balanceTypeKey").value(hasItem(DEFAULT_TYPE_KEY)))
            .andExpect(jsonPath("$.[*].balanceId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].operationType").value(hasItem(OperationType.RELOAD.toString())))
            .andExpect(jsonPath("$.[*].amountAfter").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].amountTotal").value(Matchers.hasItem(DEFAULT_AMOUNT.intValue())))
            .andDo(print());
    }

}
