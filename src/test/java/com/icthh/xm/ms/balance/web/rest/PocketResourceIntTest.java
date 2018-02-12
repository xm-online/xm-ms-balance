package com.icthh.xm.ms.balance.web.rest;

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
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.PocketService;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Test class for the PocketResource REST controller.
 *
 * @see PocketResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BalanceApp.class, SecurityBeanOverrideConfiguration.class})
public class PocketResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_KEY = "BBBBBBBBBB";

    private static final Instant DEFAULT_START_DATE_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_RESERVED = new BigDecimal(1);
    private static final BigDecimal UPDATED_RESERVED = new BigDecimal(2);

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private PocketService pocketService;

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

    private MockMvc restPocketMockMvc;

    private Pocket pocket;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PocketResource pocketResource = new PocketResource(pocketService);
        this.restPocketMockMvc = MockMvcBuilders.standaloneSetup(pocketResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pocket createEntity(EntityManager em) {
        Pocket pocket = new Pocket()
            .key(DEFAULT_KEY)
            .typeKey(DEFAULT_TYPE_KEY)
            .startDateTime(DEFAULT_START_DATE_TIME)
            .endDateTime(DEFAULT_END_DATE_TIME)
            .amount(DEFAULT_AMOUNT)
            .reserved(DEFAULT_RESERVED);
        return pocket;
    }

    @Before
    public void initTest() {
        pocket = createEntity(em);
    }

    @Test
    @Transactional
    public void createPocket() throws Exception {
        int databaseSizeBeforeCreate = pocketRepository.findAll().size();

        // Create the Pocket
        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocket)))
            .andExpect(status().isCreated());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeCreate + 1);
        Pocket testPocket = pocketList.get(pocketList.size() - 1);
        assertThat(testPocket.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testPocket.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testPocket.getStartDateTime()).isEqualTo(DEFAULT_START_DATE_TIME);
        assertThat(testPocket.getEndDateTime()).isEqualTo(DEFAULT_END_DATE_TIME);
        assertThat(testPocket.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testPocket.getReserved()).isEqualTo(DEFAULT_RESERVED);
    }

    @Test
    @Transactional
    public void createPocketWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pocketRepository.findAll().size();

        // Create the Pocket with an existing ID
        pocket.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocket)))
            .andExpect(status().isBadRequest());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = pocketRepository.findAll().size();
        // set the field null
        pocket.setKey(null);

        // Create the Pocket, which fails.

        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocket)))
            .andExpect(status().isBadRequest());

        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = pocketRepository.findAll().size();
        // set the field null
        pocket.setTypeKey(null);

        // Create the Pocket, which fails.

        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocket)))
            .andExpect(status().isBadRequest());

        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPockets() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList
        restPocketMockMvc.perform(get("/api/pockets?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pocket.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].startDateTime").value(hasItem(DEFAULT_START_DATE_TIME.toString())))
            .andExpect(jsonPath("$.[*].endDateTime").value(hasItem(DEFAULT_END_DATE_TIME.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].reserved").value(hasItem(DEFAULT_RESERVED.intValue())));
    }

    @Test
    @Transactional
    public void getPocket() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get the pocket
        restPocketMockMvc.perform(get("/api/pockets/{id}", pocket.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(pocket.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.startDateTime").value(DEFAULT_START_DATE_TIME.toString()))
            .andExpect(jsonPath("$.endDateTime").value(DEFAULT_END_DATE_TIME.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPocket() throws Exception {
        // Get the pocket
        restPocketMockMvc.perform(get("/api/pockets/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePocket() throws Exception {
        // Initialize the database
        pocketService.save(pocket);

        int databaseSizeBeforeUpdate = pocketRepository.findAll().size();

        // Update the pocket
        Pocket updatedPocket = pocketRepository.findOne(pocket.getId());
        updatedPocket
            .key(UPDATED_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .startDateTime(UPDATED_START_DATE_TIME)
            .endDateTime(UPDATED_END_DATE_TIME)
            .amount(UPDATED_AMOUNT)
            .reserved(UPDATED_RESERVED);

        restPocketMockMvc.perform(put("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPocket)))
            .andExpect(status().isOk());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeUpdate);
        Pocket testPocket = pocketList.get(pocketList.size() - 1);
        assertThat(testPocket.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testPocket.getTypeKey()).isEqualTo(UPDATED_TYPE_KEY);
        assertThat(testPocket.getStartDateTime()).isEqualTo(UPDATED_START_DATE_TIME);
        assertThat(testPocket.getEndDateTime()).isEqualTo(UPDATED_END_DATE_TIME);
        assertThat(testPocket.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testPocket.getReserved()).isEqualTo(UPDATED_RESERVED);
    }

    @Test
    @Transactional
    public void updateNonExistingPocket() throws Exception {
        int databaseSizeBeforeUpdate = pocketRepository.findAll().size();

        // Create the Pocket

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPocketMockMvc.perform(put("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocket)))
            .andExpect(status().isCreated());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePocket() throws Exception {
        // Initialize the database
        pocketService.save(pocket);

        int databaseSizeBeforeDelete = pocketRepository.findAll().size();

        // Get the pocket
        restPocketMockMvc.perform(delete("/api/pockets/{id}", pocket.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pocket.class);
        Pocket pocket1 = new Pocket();
        pocket1.setId(1L);
        Pocket pocket2 = new Pocket();
        pocket2.setId(pocket1.getId());
        assertThat(pocket1).isEqualTo(pocket2);
        pocket2.setId(2L);
        assertThat(pocket1).isNotEqualTo(pocket2);
        pocket1.setId(null);
        assertThat(pocket1).isNotEqualTo(pocket2);
    }
}
