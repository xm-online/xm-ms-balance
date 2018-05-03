package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.ms.balance.BalanceApp;

import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.commons.exceptions.spring.web.ExceptionTranslator;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;
import com.icthh.xm.ms.balance.service.BalanceQueryService;

import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.icthh.xm.ms.balance.web.rest.TestUtil.createFormattingConversionService;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the BalanceResource REST controller.
 *
 * @see BalanceResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BalanceApp.class, SecurityBeanOverrideConfiguration.class})
public class BalanceResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_MEASURE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_MEASURE_KEY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_RESERVED = new BigDecimal(1);
    private static final BigDecimal UPDATED_RESERVED = new BigDecimal(2);

    private static final Long DEFAULT_ENTITY_ID = 1L;
    private static final Long UPDATED_ENTITY_ID = 2L;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final BigDecimal AMOUNT = new BigDecimal("55.0");

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

    private MockMvc restBalanceMockMvc;

    private Balance balance;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BalanceResource balanceResource = new BalanceResource(balanceService, balanceQueryService);
        this.restBalanceMockMvc = MockMvcBuilders.standaloneSetup(balanceResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Balance createEntity(EntityManager em) {
        Balance balance = new Balance()
            .key(DEFAULT_KEY)
            .typeKey(DEFAULT_TYPE_KEY)
            .measureKey(DEFAULT_MEASURE_KEY)
            .reserved(DEFAULT_RESERVED)
            .entityId(DEFAULT_ENTITY_ID)
            .createdBy(DEFAULT_CREATED_BY);
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
        assertThat(testBalance.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testBalance.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testBalance.getMeasureKey()).isEqualTo(DEFAULT_MEASURE_KEY);
        assertThat(testBalance.getReserved()).isEqualTo(DEFAULT_RESERVED);
        assertThat(testBalance.getEntityId()).isEqualTo(DEFAULT_ENTITY_ID);
        assertThat(testBalance.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
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

    @Test
    @Transactional
    @Ignore("Filter by reference entity is not supported due to dynamic permission (see PermittedRepository.findByCondition)")
    public void getAllBalancesByPocketsIsEqualToSomething() throws Exception {
        // Initialize the database
        Pocket pockets = PocketResourceIntTest.createEntity(em);
        em.persist(pockets);
        em.flush();
        balance.addPockets(pockets);
        balanceRepository.saveAndFlush(balance);
        Long pocketsId = pockets.getId();

        // Get all the balanceList where pockets equals to pocketsId
        defaultBalanceShouldBeFound("pocketsId.equals=" + pocketsId);

        // Get all the balanceList where pockets equals to pocketsId + 1
        defaultBalanceShouldNotBeFound("pocketsId.equals=" + (pocketsId + 1));
    }


    @Test
    @Transactional
    @Ignore("Filter by reference entity is not supported due to dynamic permission (see PermittedRepository.findByCondition)")
    public void getAllBalancesByMetricsIsEqualToSomething() throws Exception {
        // Initialize the database
        Metric metrics = MetricResourceIntTest.createEntity(em);
        em.persist(metrics);
        em.flush();
        balance.addMetrics(metrics);
        balanceRepository.saveAndFlush(balance);
        Long metricsId = metrics.getId();

        // Get all the balanceList where metrics equals to metricsId
        defaultBalanceShouldBeFound("metricsId.equals=" + metricsId);

        // Get all the balanceList where metrics equals to metricsId + 1
        defaultBalanceShouldNotBeFound("metricsId.equals=" + (metricsId + 1));
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
        Balance updatedBalance = balanceRepository.findOne(balance.getId());
        // Disconnect from session so that the updates on updatedBalance are not directly saved in db
        em.detach(updatedBalance);
        updatedBalance
            .key(UPDATED_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .measureKey(UPDATED_MEASURE_KEY)
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
            .endDateTime(now().plusSeconds(500)).typeKey("TYPEKEY").key("KEY");
        Pocket pocket1 = new Pocket().amount(new BigDecimal("50")).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().plusSeconds(500)).typeKey("TYPEKEY").key("KEY");
        Pocket expired = new Pocket().amount(new BigDecimal("50")).balance(balance).startDateTime(now().minusSeconds(500))
            .endDateTime(now().minusSeconds(500)).typeKey("TYPEKEY").key("KEY");
        pocketRepository.saveAndFlush(pocket);
        pocketRepository.saveAndFlush(pocket1);
        pocketRepository.saveAndFlush(expired);
    }

}
