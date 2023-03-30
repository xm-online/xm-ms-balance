package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.lep.XmLepScriptConfigServerResourceLoader;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepositoryIntTest;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.BalanceQueryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.BalanceSpecService;
import com.icthh.xm.ms.balance.service.OperationType;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.util.LinkedMultiValueMap;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepScriptConstants.BINDING_KEY_AUTH_CONTEXT;
import static com.icthh.xm.ms.balance.web.rest.TestUtil.createFormattingConversionService;
import static java.math.BigDecimal.TEN;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the BalanceResource REST controller.
 *
 * @see BalanceResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SecurityBeanOverrideConfiguration.class, BalanceApp.class})
public class BalanceResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_MEASURE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_MEASURE_KEY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_RESERVED = new BigDecimal(1);
    private static final BigDecimal UPDATED_RESERVED = new BigDecimal(2);

    private static final Long DEFAULT_ENTITY_ID = 1L;
    private static final Long UPDATED_ENTITY_ID = 2L;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String DEFAULT_STATUS = "ACTIVE";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final BigDecimal AMOUNT = new BigDecimal("55.0");
    private static final String BALANCE_TYPE_KEY = "BALANCE";
    private static final String RELOAD_DEFAULT_VALUE = "200";
    private static final String RELOAD = "reload";
    private static final String CHARGE = "charge";
    private static final String CHARGE_DEFAULT_VALUE = "60";
    private static final String RELOAD_FUTURE_DEFAULT_VALUE = "300";
    private static final String RELOAD_EXPIRED_DEFAULT_VALUE = "175";

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private BalanceQueryService balanceQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private BalanceChangeEventRepository balanceChangeEventRepository;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmAuthenticationContextHolder xmAuthenticationContextHolder;

    @Autowired
    private XmLepScriptConfigServerResourceLoader leps;

    @Autowired
    private BalanceSpecService balanceSpecService;

    private MockMvc restBalanceMockMvc;

    private Balance balance;

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
        final BalanceResource balanceResource = new BalanceResource(balanceService, balanceQueryService);
        this.restBalanceMockMvc = MockMvcBuilders.standaloneSetup(balanceResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
        String balanceSpec = new String(BalanceRepositoryIntTest.class.getResourceAsStream("/config/balancespec.yml").readAllBytes());
        balanceSpecService.onRefresh("/config/tenants/RESINTTEST/balance/balancespec.yml", balanceSpec);
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Balance createEntity(EntityManager em) {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(DEFAULT_TYPE_KEY)
            .measureKey(DEFAULT_MEASURE_KEY)
            .amount(DEFAULT_AMOUNT)
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY)
            .status("active");
        return balance;
    }

    @Before
    public void initTest() {
        balance = createEntity(em);
    }

    @Test
    @Transactional
    public void createBalance() throws Exception {
        int databaseSizeBeforeCreate = balanceRepository.findAll().size();

        // Create the Balance
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);
        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isCreated());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeCreate + 1);
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        log.info("{}", testBalance);
        assertThat(testBalance.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testBalance.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testBalance.getMeasureKey()).isEqualTo(DEFAULT_MEASURE_KEY);
        assertThat(testBalance.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testBalance.getReserved()).isEqualTo(DEFAULT_RESERVED);
        assertThat(testBalance.getEntityId()).isEqualTo(DEFAULT_ENTITY_ID);
        assertThat(testBalance.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testBalance.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createBalanceWithoutStatus() throws Exception {
        int databaseSizeBeforeCreate = balanceRepository.findAll().size();

        // Create the Balance
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);
        balanceDTO.setStatus(null);
        restBalanceMockMvc.perform(post("/api/balances")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isCreated());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeCreate + 1);
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        log.info("{}", testBalance);
        assertThat(testBalance.getStatus()).isNull();
    }

    @Test
    @Transactional
    public void createBalanceWitUnsupportedStatus() throws Exception {
        // Create the Balance
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);
        balanceDTO.setStatus("TEST");
        balanceDTO.setTypeKey("BALANCE");
        restBalanceMockMvc.perform(post("/api/balances")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertEquals("Unsupported status [TEST] for type key: BALANCE",
                result.getResolvedException().getMessage()));
    }

    @Test
    @Transactional
    public void createBalanceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = balanceRepository.findAll().size();

        // Create the Balance with an existing ID
        balance.setId(1L);
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceRepository.findAll().size();
        // set the field null
        balance.setKey(null);

        // Create the Balance, which fails.
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isBadRequest());

        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceRepository.findAll().size();
        // set the field null
        balance.setTypeKey(null);

        // Create the Balance, which fails.
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isBadRequest());

        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEntityIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = balanceRepository.findAll().size();
        // set the field null
        balance.setEntityId(null);

        // Create the Balance, which fails.
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isBadRequest());

        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllBalances() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        expectBalanceHasPockets(balance);

        // Get all the balanceList
        restBalanceMockMvc.perform(get("/api/balances?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(balance.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].measureKey").value(hasItem(DEFAULT_MEASURE_KEY.toString())))
            .andExpect(jsonPath("$.[*].reserved").value(hasItem(DEFAULT_RESERVED.intValue())))
            .andExpect(jsonPath("$.[*].entityId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(AMOUNT.doubleValue())))
            .andDo(print());
    }

    @Test
    @Transactional
    public void getBalance() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        expectBalanceHasPockets(balance);

        // Get the balance
        restBalanceMockMvc.perform(get("/api/balances/{id}", balance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(balance.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.measureKey").value(DEFAULT_MEASURE_KEY.toString()))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()))
            .andExpect(jsonPath("$.entityId").value(DEFAULT_ENTITY_ID.intValue()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY.toString()))
            .andExpect(jsonPath("$.amount").value(AMOUNT.doubleValue()))
            .andDo(print());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where key equals to DEFAULT_KEY
        defaultBalanceShouldBeFound("key.equals=" + DEFAULT_KEY);

        // Get all the balanceList where key equals to UPDATED_KEY
        defaultBalanceShouldNotBeFound("key.equals=" + UPDATED_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByKeyIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where key in DEFAULT_KEY or UPDATED_KEY
        defaultBalanceShouldBeFound("key.in=" + DEFAULT_KEY + "," + UPDATED_KEY);

        // Get all the balanceList where key equals to UPDATED_KEY
        defaultBalanceShouldNotBeFound("key.in=" + UPDATED_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where key is not null
        defaultBalanceShouldBeFound("key.specified=true");

        // Get all the balanceList where key is null
        defaultBalanceShouldNotBeFound("key.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByTypeKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where typeKey equals to DEFAULT_TYPE_KEY
        defaultBalanceShouldBeFound("typeKey.equals=" + DEFAULT_TYPE_KEY);

        // Get all the balanceList where typeKey equals to UPDATED_TYPE_KEY
        defaultBalanceShouldNotBeFound("typeKey.equals=" + UPDATED_TYPE_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByTypeKeyIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where typeKey in DEFAULT_TYPE_KEY or UPDATED_TYPE_KEY
        defaultBalanceShouldBeFound("typeKey.in=" + DEFAULT_TYPE_KEY + "," + UPDATED_TYPE_KEY);

        // Get all the balanceList where typeKey equals to UPDATED_TYPE_KEY
        defaultBalanceShouldNotBeFound("typeKey.in=" + UPDATED_TYPE_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByTypeKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where typeKey is not null
        defaultBalanceShouldBeFound("typeKey.specified=true");

        // Get all the balanceList where typeKey is null
        defaultBalanceShouldNotBeFound("typeKey.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByMeasureKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where measureKey equals to DEFAULT_MEASURE_KEY
        defaultBalanceShouldBeFound("measureKey.equals=" + DEFAULT_MEASURE_KEY);

        // Get all the balanceList where measureKey equals to UPDATED_MEASURE_KEY
        defaultBalanceShouldNotBeFound("measureKey.equals=" + UPDATED_MEASURE_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByMeasureKeyIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where measureKey in DEFAULT_MEASURE_KEY or UPDATED_MEASURE_KEY
        defaultBalanceShouldBeFound("measureKey.in=" + DEFAULT_MEASURE_KEY + "," + UPDATED_MEASURE_KEY);

        // Get all the balanceList where measureKey equals to UPDATED_MEASURE_KEY
        defaultBalanceShouldNotBeFound("measureKey.in=" + UPDATED_MEASURE_KEY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByMeasureKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where measureKey is not null
        defaultBalanceShouldBeFound("measureKey.specified=true");

        // Get all the balanceList where measureKey is null
        defaultBalanceShouldNotBeFound("measureKey.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where amount equals to DEFAULT_AMOUNT
        defaultBalanceShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the balanceList where amount equals to UPDATED_AMOUNT
        defaultBalanceShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultBalanceShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the balanceList where amount equals to UPDATED_AMOUNT
        defaultBalanceShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where amount is not null
        defaultBalanceShouldBeFound("amount.specified=true");

        // Get all the balanceList where amount is null
        defaultBalanceShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByReservedIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where reserved equals to DEFAULT_RESERVED
        defaultBalanceShouldBeFound("reserved.equals=" + DEFAULT_RESERVED);

        // Get all the balanceList where reserved equals to UPDATED_RESERVED
        defaultBalanceShouldNotBeFound("reserved.equals=" + UPDATED_RESERVED);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByReservedIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where reserved in DEFAULT_RESERVED or UPDATED_RESERVED
        defaultBalanceShouldBeFound("reserved.in=" + DEFAULT_RESERVED + "," + UPDATED_RESERVED);

        // Get all the balanceList where reserved equals to UPDATED_RESERVED
        defaultBalanceShouldNotBeFound("reserved.in=" + UPDATED_RESERVED);
    }

    @WithMockUser(authorities = "SUPER-ADMIN")
    @Test
    @Transactional
    public void getAllBalancesByReservedIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where reserved is not null
        defaultBalanceShouldBeFound("reserved.specified=true");

        // Get all the balanceList where reserved is null
        defaultBalanceShouldNotBeFound("reserved.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByEntityIdIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where entityId equals to DEFAULT_ENTITY_ID
        defaultBalanceShouldBeFound("entityId.equals=" + DEFAULT_ENTITY_ID);

        // Get all the balanceList where entityId equals to UPDATED_ENTITY_ID
        defaultBalanceShouldNotBeFound("entityId.equals=" + UPDATED_ENTITY_ID);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByEntityIdIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where entityId in DEFAULT_ENTITY_ID or UPDATED_ENTITY_ID
        defaultBalanceShouldBeFound("entityId.in=" + DEFAULT_ENTITY_ID + "," + UPDATED_ENTITY_ID);

        // Get all the balanceList where entityId equals to UPDATED_ENTITY_ID
        defaultBalanceShouldNotBeFound("entityId.in=" + UPDATED_ENTITY_ID);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByEntityIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where entityId is not null
        defaultBalanceShouldBeFound("entityId.specified=true");

        // Get all the balanceList where entityId is null
        defaultBalanceShouldNotBeFound("entityId.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByEntityIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where entityId greater than or equals to DEFAULT_ENTITY_ID
        defaultBalanceShouldBeFound("entityId.greaterOrEqualThan=" + DEFAULT_ENTITY_ID);

        // Get all the balanceList where entityId greater than or equals to UPDATED_ENTITY_ID
        defaultBalanceShouldNotBeFound("entityId.greaterOrEqualThan=" + UPDATED_ENTITY_ID);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByEntityIdIsLessThanSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where entityId less than or equals to DEFAULT_ENTITY_ID
        defaultBalanceShouldNotBeFound("entityId.lessThan=" + DEFAULT_ENTITY_ID);

        // Get all the balanceList where entityId less than or equals to UPDATED_ENTITY_ID
        defaultBalanceShouldBeFound("entityId.lessThan=" + UPDATED_ENTITY_ID);
    }


    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByCreatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where createdBy equals to DEFAULT_CREATED_BY
        defaultBalanceShouldBeFound("createdBy.equals=" + DEFAULT_CREATED_BY);

        // Get all the balanceList where createdBy equals to UPDATED_CREATED_BY
        defaultBalanceShouldNotBeFound("createdBy.equals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByCreatedByIsInShouldWork() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where createdBy in DEFAULT_CREATED_BY or UPDATED_CREATED_BY
        defaultBalanceShouldBeFound("createdBy.in=" + DEFAULT_CREATED_BY + "," + UPDATED_CREATED_BY);

        // Get all the balanceList where createdBy equals to UPDATED_CREATED_BY
        defaultBalanceShouldNotBeFound("createdBy.in=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "SUPER-ADMIN")
    public void getAllBalancesByCreatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get all the balanceList where createdBy is not null
        defaultBalanceShouldBeFound("createdBy.specified=true");

        // Get all the balanceList where createdBy is null
        defaultBalanceShouldNotBeFound("createdBy.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultBalanceShouldBeFound(String filter) throws Exception {
        restBalanceMockMvc.perform(get("/api/balances?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(balance.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].measureKey").value(hasItem(DEFAULT_MEASURE_KEY.toString())))
            .andExpect(jsonPath("$.[*].reserved").value(hasItem(DEFAULT_RESERVED.intValue())))
            .andExpect(jsonPath("$.[*].entityId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultBalanceShouldNotBeFound(String filter) throws Exception {
        restBalanceMockMvc.perform(get("/api/balances?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getNonExistingBalance() throws Exception {
        // Get the balance
        restBalanceMockMvc.perform(get("/api/balances/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBalance() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);
        int databaseSizeBeforeUpdate = balanceRepository.findAll().size();

        // Update the balance
        Balance updatedBalance = balanceRepository.findById(balance.getId())
            .orElseThrow(() -> new IllegalStateException("Balance not found for id " + balance.getId()));
        // Disconnect from session so that the updates on updatedBalance are not directly saved in db
        em.detach(updatedBalance);
        updatedBalance
            .key(UPDATED_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .measureKey(UPDATED_MEASURE_KEY)
            .amount(UPDATED_AMOUNT)
            .reserved(UPDATED_RESERVED)
            .entityId(UPDATED_ENTITY_ID)
            .createdBy(UPDATED_CREATED_BY);
        BalanceDTO balanceDTO = balanceMapper.toDto(updatedBalance);

        restBalanceMockMvc.perform(put("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isOk());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeUpdate);
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        assertThat(testBalance.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testBalance.getTypeKey()).isEqualTo(UPDATED_TYPE_KEY);
        assertThat(testBalance.getMeasureKey()).isEqualTo(UPDATED_MEASURE_KEY);
        assertThat(testBalance.getReserved()).isEqualTo(UPDATED_RESERVED);
        assertThat(testBalance.getEntityId()).isEqualTo(UPDATED_ENTITY_ID);
        assertThat(testBalance.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    public void updateNonExistingBalance() throws Exception {
        int databaseSizeBeforeUpdate = balanceRepository.findAll().size();

        // Create the Balance
        BalanceDTO balanceDTO = balanceMapper.toDto(balance);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restBalanceMockMvc.perform(put("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balanceDTO)))
            .andExpect(status().isCreated());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteBalance() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);
        int databaseSizeBeforeDelete = balanceRepository.findAll().size();

        // Get the balance
        restBalanceMockMvc.perform(delete("/api/balances/{id}", balance.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Balance.class);
        Balance balance1 = new Balance();
        balance1.setId(1L);
        Balance balance2 = new Balance();
        balance2.setId(balance1.getId());
        assertThat(balance1).isEqualTo(balance2);
        balance2.setId(2L);
        assertThat(balance1).isNotEqualTo(balance2);
        balance1.setId(null);
        assertThat(balance1).isNotEqualTo(balance2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BalanceDTO.class);
        BalanceDTO balanceDTO1 = new BalanceDTO();
        balanceDTO1.setId(1L);
        BalanceDTO balanceDTO2 = new BalanceDTO();
        assertThat(balanceDTO1).isNotEqualTo(balanceDTO2);
        balanceDTO2.setId(balanceDTO1.getId());
        assertThat(balanceDTO1).isEqualTo(balanceDTO2);
        balanceDTO2.setId(2L);
        assertThat(balanceDTO1).isNotEqualTo(balanceDTO2);
        balanceDTO1.setId(null);
        assertThat(balanceDTO1).isNotEqualTo(balanceDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(balanceMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(balanceMapper.fromId(null)).isNull();
    }

    private void expectBalanceHasPockets(Balance balance) {
        Pocket pocket = new Pocket().amount(new BigDecimal("5")).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().plusSeconds(500)).label("LABEL1").key("KEY");
        Pocket pocket1 = new Pocket().amount(new BigDecimal("50")).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().plusSeconds(500)).label("LABEL2").key("KEY");
        Pocket expired = new Pocket().amount(new BigDecimal("50")).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().minusSeconds(500)).label("LABEL4").key("KEY");
        pocketRepository.saveAndFlush(pocket);
        pocketRepository.saveAndFlush(pocket1);
        pocketRepository.saveAndFlush(expired);
    }

    @Test
    @Transactional
    public void getBalanceWithApplyDate() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        expectBalanceHasPockets(balance);

        // Get the balance
        restBalanceMockMvc.perform(get("/api/balances/{id}", balance.getId()).param("applyDate", Instant.now().toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(balance.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY))
            .andExpect(jsonPath("$.measureKey").value(DEFAULT_MEASURE_KEY))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()))
            .andExpect(jsonPath("$.entityId").value(DEFAULT_ENTITY_ID.intValue()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.amount").value(AMOUNT.doubleValue()))
            .andDo(print());
    }

    @Test
    @Transactional
    public void getBalanceAmountWithWrongApplyDate() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        expectBalanceHasPockets(balance);

        // Get the balance
        restBalanceMockMvc.perform(get("/api/balances/{id}", balance.getId()).param("applyDate", Instant.now().plusSeconds(1000).toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(balance.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY))
            .andExpect(jsonPath("$.measureKey").value(DEFAULT_MEASURE_KEY))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()))
            .andExpect(jsonPath("$.entityId").value(DEFAULT_ENTITY_ID.intValue()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.amount").value(0))
            .andDo(print());
    }


    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void chargeWithPocketHistoryAndSameOperationId() throws Exception {

        BigDecimal balanceAmount = new BigDecimal("60.0");
        BigDecimal chargingAmount = new BigDecimal("100.0");

        // Initialize the database
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(BALANCE_TYPE_KEY)
            .measureKey("UNIT")
            .amount(DEFAULT_AMOUNT)
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY);

        Pocket pocket = new Pocket().amount(balanceAmount).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().plusSeconds(500)).label("LABEL1").key("KEY");
        balanceRepository.saveAndFlush(balance);
        pocketRepository.saveAndFlush(pocket);

        String uuid = UUID.randomUUID().toString();
        ChargingBalanceRequest request = new ChargingBalanceRequest(balance.getId(), chargingAmount);
        request.setWithAffectedPocketHistory(true);
        request.setChargeAsManyAsPossible(true);
        request.setUuid(uuid);

        restBalanceMockMvc.perform(post("/api/balances/charging")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balanceId").value(balance.getId()))
            .andExpect(jsonPath("$.amountAfter").value("0"))
            .andExpect(jsonPath("$.amountBefore").value("60.0"))
            .andExpect(jsonPath("$.amountDelta").value("60.0"))
            .andExpect(jsonPath("$.operationId").value(uuid))
            .andExpect(jsonPath("$.amountTotal").value("100.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountBefore").value("60.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].pocketLabel").value("LABEL1"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountDelta").value("60.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountAfter").value("0.0"));

        ChargingBalanceRequest sameRequest = new ChargingBalanceRequest(balance.getId(), TEN);
        sameRequest.setWithAffectedPocketHistory(true);
        sameRequest.setChargeAsManyAsPossible(true);
        sameRequest.setUuid(uuid);

        restBalanceMockMvc.perform(post("/api/balances/charging")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sameRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balanceId").value(balance.getId()))
            .andExpect(jsonPath("$.amountAfter").value("0"))
            .andExpect(jsonPath("$.amountBefore").value("60.0"))
            .andExpect(jsonPath("$.amountDelta").value("60.0"))
            .andExpect(jsonPath("$.operationId").value(uuid))
            .andExpect(jsonPath("$.amountTotal").value("100.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountBefore").value("60.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].pocketLabel").value("LABEL1"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountDelta").value("60.0"))
            .andExpect(jsonPath("$.pocketChangeEvents.[0].amountAfter").value("0.0"));
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void changeBalanceStatusWhenTransitionAllowed() throws Exception {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(BALANCE_TYPE_KEY)
            .measureKey("UNIT")
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY)
            .status(DEFAULT_STATUS);

        balance = balanceRepository.saveAndFlush(balance);
        assertThat(balance.getStatus()).isEqualTo(DEFAULT_STATUS);

        ReloadBalanceRequest reloadBalanceRequest = buildReloadBalanceRequest(balance);
        balanceService.reload(reloadBalanceRequest);

        restBalanceMockMvc.perform(put("/api/balances/{id}/statuses/{status}", balance.getId(), "blocked")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new HashMap<>())))
            .andExpect(status().isOk());

        List<Balance> balanceList = balanceRepository.findAll();
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        log.info("{}", testBalance);
        assertThat(testBalance.getStatus()).isEqualTo("BLOCKED");

        List<BalanceChangeEvent> changeEvents = balanceChangeEventRepository.findAll();
        log.info("changeEvents: {}", changeEvents);
        BalanceChangeEvent testChangeEvents = changeEvents.get(changeEvents.size() - 1);

        assertThat(testChangeEvents.getMetadata().getMetadata()).isEqualTo(expectedMetadataMap());
        assertThat(testChangeEvents.getOperationType()).isEqualTo(OperationType.CHANGE_STATUS);
        assertThat(testChangeEvents.getBalanceId()).isEqualTo(balance.getId());
        assertThat(testChangeEvents.getBalanceKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testChangeEvents.getBalanceTypeKey()).isEqualTo(BALANCE_TYPE_KEY);
        BigDecimal reloadDefault = new BigDecimal(RELOAD_DEFAULT_VALUE).setScale(2, RoundingMode.HALF_UP);
        assertThat(testChangeEvents.getAmountBefore()).isEqualTo(reloadDefault);
        assertThat(testChangeEvents.getAmountAfter()).isEqualTo(reloadDefault);
        assertThat(testChangeEvents.getAmountDelta()).isEqualTo(BigDecimal.ZERO);
        assertThat(testChangeEvents.getAmountTotal()).isEqualTo(BigDecimal.ZERO);
    }

    @NotNull
    private static ReloadBalanceRequest buildReloadBalanceRequest(Balance balance) {
        ReloadBalanceRequest reloadBalanceRequest = new ReloadBalanceRequest();
        reloadBalanceRequest.setBalanceId(balance.getId());
        reloadBalanceRequest.setAmount(new BigDecimal(RELOAD_DEFAULT_VALUE));
        Map<String, String> reloadMetadata = new HashMap<>();
        reloadMetadata.put(RELOAD, RELOAD_DEFAULT_VALUE);
        reloadBalanceRequest.setMetadata(reloadMetadata);
        reloadBalanceRequest.setLabel(RELOAD);
        return reloadBalanceRequest;
    }

    private Map<String, String> expectedMetadataMap() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("change_status_from", "ACTIVE");
        metadata.put("change_status_to", "BLOCKED");
        return metadata;
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void changeBalanceStatusWhenTransitionNotAllowed() throws Exception {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(BALANCE_TYPE_KEY)
            .measureKey("UNIT")
            .amount(DEFAULT_AMOUNT)
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY)
            .status(DEFAULT_STATUS);

        balance = balanceRepository.saveAndFlush(balance);
        assertThat(balance.getStatus()).isEqualTo(DEFAULT_STATUS);

        restBalanceMockMvc.perform(put("/api/balances/{id}/statuses/{status}", balance.getId(), "closed")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new HashMap<>())))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertEquals("Type key: BALANCE can not go from [ACTIVE] to [CLOSED]",
                result.getResolvedException().getMessage()));

        List<Balance> balanceList = balanceRepository.findAll();
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        log.info("{}", testBalance);
        assertThat(testBalance.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void changeBalanceStatusWhenCurrentBalanceStatusIsNull() throws Exception {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(BALANCE_TYPE_KEY)
            .measureKey("UNIT")
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY);

        balance = balanceRepository.saveAndFlush(balance);
        assertThat(balance.getStatus()).isNull();

        ReloadBalanceRequest reloadBalanceRequest = buildReloadBalanceRequest(balance);
        balanceService.reload(reloadBalanceRequest);

        restBalanceMockMvc.perform(put("/api/balances/{id}/statuses/{status}", balance.getId(), "active")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new HashMap<>())))
            .andExpect(status().isOk());

        List<Balance> balanceList = balanceRepository.findAll();
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        log.info("{}", testBalance);
        assertThat(testBalance.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getBalanceInfoSuccessResponse() throws Exception {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(BALANCE_TYPE_KEY)
            .measureKey("UNIT")
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY);

        balance = balanceRepository.saveAndFlush(balance);
        assertThat(balance.getStatus()).isNull();

        ReloadBalanceRequest reloadBalanceRequest = buildReloadBalanceRequest(balance);
        balanceService.reload(reloadBalanceRequest);
        balanceService.reload(reloadBalanceRequest);
        ReloadBalanceRequest reloadFutureBalanceRequest = buildFutureReloadBalanceRequest(balance);
        balanceService.reload(reloadFutureBalanceRequest);
        ReloadBalanceRequest reloadExpiredBalanceRequest = buildExpiredReloadBalanceRequest(balance);
        balanceService.reload(reloadExpiredBalanceRequest);
        ChargingBalanceRequest chargingBalanceRequest = buildChargeBalanceRequest(balance);
        balanceService.charging(chargingBalanceRequest);
        balanceService.charging(chargingBalanceRequest);

        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("fields", "activeAmount,spentAmount,futureAmount,expiredAmount,expireByDateTime");
        requestParams.add("params", "expireByDateTime=" + Instant.now().plus(20, ChronoUnit.DAYS).toString() +
            ",testParameter=testValue");

        restBalanceMockMvc.perform(get("/api/balances/{id}/info", balance.getId())
                .params(requestParams)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new HashMap<>())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(balance.getId()))
            .andExpect(jsonPath("$.activeAmount").value(280))
            .andExpect(jsonPath("$.futureAmount").value(300))
            .andExpect(jsonPath("$.spentAmount").value(120))
            .andExpect(jsonPath("$.expiredAmount").value(175))
            .andExpect(jsonPath("$.expireByDateTime").value(300))
            .andReturn();
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getBalanceInfoNotFoundResponse() throws Exception {
        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("fields", "activeAmount,spentAmount,futureAmount,expiredAmount,expireByDateTime");
        requestParams.add("params", "expireByDateTime=" + Instant.now().plus(20, ChronoUnit.DAYS).toString() +
            ",testParameter=testValue");

        restBalanceMockMvc.perform(get("/api/balances/{id}/info", "9001")
                .params(requestParams)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new HashMap<>())))
            .andExpect(status().isNotFound());
    }

    @NotNull
    private static ReloadBalanceRequest buildFutureReloadBalanceRequest(Balance balance) {
        ReloadBalanceRequest reloadBalanceRequest = new ReloadBalanceRequest();
        reloadBalanceRequest.setBalanceId(balance.getId());
        reloadBalanceRequest.setAmount(new BigDecimal(RELOAD_FUTURE_DEFAULT_VALUE));
        Map<String, String> reloadMetadata = new HashMap<>();
        reloadMetadata.put(RELOAD, RELOAD_FUTURE_DEFAULT_VALUE);
        reloadBalanceRequest.setMetadata(reloadMetadata);
        reloadBalanceRequest.setLabel(RELOAD);
        reloadBalanceRequest.setStartDateTime(Instant.now().plus(3, ChronoUnit.DAYS));
        reloadBalanceRequest.setEndDateTime(Instant.now().plus(10, ChronoUnit.DAYS));
        return reloadBalanceRequest;
    }

    @NotNull
    private static ReloadBalanceRequest buildExpiredReloadBalanceRequest(Balance balance) {
        ReloadBalanceRequest reloadBalanceRequest = new ReloadBalanceRequest();
        reloadBalanceRequest.setBalanceId(balance.getId());
        reloadBalanceRequest.setAmount(new BigDecimal(RELOAD_EXPIRED_DEFAULT_VALUE));
        Map<String, String> reloadMetadata = new HashMap<>();
        reloadMetadata.put(RELOAD, RELOAD_EXPIRED_DEFAULT_VALUE);
        reloadBalanceRequest.setMetadata(reloadMetadata);
        reloadBalanceRequest.setLabel(RELOAD);
        reloadBalanceRequest.setStartDateTime(Instant.now().plus(-4, ChronoUnit.DAYS));
        reloadBalanceRequest.setEndDateTime(Instant.now().plus(-3, ChronoUnit.DAYS));
        return reloadBalanceRequest;
    }

    @NotNull
    private static ChargingBalanceRequest buildChargeBalanceRequest(Balance balance) {
        ChargingBalanceRequest chargingBalanceRequest = new ChargingBalanceRequest();
        chargingBalanceRequest.setBalanceId(balance.getId());
        chargingBalanceRequest.setAmount(new BigDecimal(CHARGE_DEFAULT_VALUE));
        Map<String, String> reloadMetadata = new HashMap<>();
        reloadMetadata.put(CHARGE, CHARGE_DEFAULT_VALUE);
        chargingBalanceRequest.setMetadata(reloadMetadata);
        return chargingBalanceRequest;
    }
}
