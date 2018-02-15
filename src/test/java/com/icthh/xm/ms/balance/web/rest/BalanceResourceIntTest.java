package com.icthh.xm.ms.balance.web.rest;

import static com.icthh.xm.ms.balance.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.icthh.xm.commons.exceptions.spring.web.ExceptionTranslator;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.service.BalanceService;
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

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;

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

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_RESERVED = new BigDecimal(1);
    private static final BigDecimal UPDATED_RESERVED = new BigDecimal(2);

    private static final Long DEFAULT_ENTITY_ID = 1L;
    private static final Long UPDATED_ENTITY_ID = 2L;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private BalanceService balanceService;

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

    private MockMvc restBalanceMockMvc;

    private Balance balance;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BalanceResource balanceResource = new BalanceResource(balanceService);
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
            .amount(DEFAULT_AMOUNT)
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
        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
            .andExpect(status().isCreated());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeCreate + 1);
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        assertThat(testBalance.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testBalance.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testBalance.getMeasureKey()).isEqualTo(DEFAULT_MEASURE_KEY);
        assertThat(testBalance.getAmount()).isEqualTo(DEFAULT_AMOUNT);
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

        // An entity with an existing ID cannot be created, so this API call must fail
        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
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

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
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

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
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

        restBalanceMockMvc.perform(post("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
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

        // Get all the balanceList
        restBalanceMockMvc.perform(get("/api/balances?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(balance.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].measureKey").value(hasItem(DEFAULT_MEASURE_KEY.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].reserved").value(hasItem(DEFAULT_RESERVED.intValue())))
            .andExpect(jsonPath("$.[*].entityId").value(hasItem(DEFAULT_ENTITY_ID.intValue())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())));
    }

    @Test
    @Transactional
    public void getBalance() throws Exception {
        // Initialize the database
        balanceRepository.saveAndFlush(balance);

        // Get the balance
        restBalanceMockMvc.perform(get("/api/balances/{id}", balance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(balance.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.measureKey").value(DEFAULT_MEASURE_KEY.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()))
            .andExpect(jsonPath("$.entityId").value(DEFAULT_ENTITY_ID.intValue()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY.toString()));
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
        balanceService.save(balance);

        int databaseSizeBeforeUpdate = balanceRepository.findAll().size();

        // Update the balance
        Balance updatedBalance = balanceRepository.findOne(balance.getId());
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

        restBalanceMockMvc.perform(put("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedBalance)))
            .andExpect(status().isOk());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeUpdate);
        Balance testBalance = balanceList.get(balanceList.size() - 1);
        assertThat(testBalance.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testBalance.getTypeKey()).isEqualTo(UPDATED_TYPE_KEY);
        assertThat(testBalance.getMeasureKey()).isEqualTo(UPDATED_MEASURE_KEY);
        assertThat(testBalance.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testBalance.getReserved()).isEqualTo(UPDATED_RESERVED);
        assertThat(testBalance.getEntityId()).isEqualTo(UPDATED_ENTITY_ID);
        assertThat(testBalance.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    public void updateNonExistingBalance() throws Exception {
        int databaseSizeBeforeUpdate = balanceRepository.findAll().size();

        // Create the Balance

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restBalanceMockMvc.perform(put("/api/balances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(balance)))
            .andExpect(status().isCreated());

        // Validate the Balance in the database
        List<Balance> balanceList = balanceRepository.findAll();
        assertThat(balanceList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteBalance() throws Exception {
        // Initialize the database
        balanceService.save(balance);

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
}
