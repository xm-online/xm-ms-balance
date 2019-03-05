package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.PocketQueryService;
import com.icthh.xm.ms.balance.service.PocketService;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import com.icthh.xm.ms.balance.service.mapper.PocketMapper;
import lombok.extern.slf4j.Slf4j;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

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

/**
 * Test class for the PocketResource REST controller.
 *
 * @see PocketResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BalanceApp.class, SecurityBeanOverrideConfiguration.class})
public class PocketResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_LABEL = "AAAAAAAAAA";
    private static final String UPDATED_LABEL = "BBBBBBBBBB";

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
    private PocketMapper pocketMapper;

    @Autowired
    private PocketService pocketService;

    @Autowired
    private PocketQueryService pocketQueryService;

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
        final PocketResource pocketResource = new PocketResource(pocketService, pocketQueryService);
        this.restPocketMockMvc = MockMvcBuilders.standaloneSetup(pocketResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pocket createEntity(EntityManager em) {
        Pocket pocket = new Pocket()
            .key(DEFAULT_KEY)
            .label(DEFAULT_LABEL)
            .startDateTime(DEFAULT_START_DATE_TIME)
            .endDateTime(DEFAULT_END_DATE_TIME)
            .amount(DEFAULT_AMOUNT)
            .reserved(DEFAULT_RESERVED);
        // Add required entity
        Balance balance = BalanceResourceIntTest.createEntity(em);
        em.persist(balance);
        em.flush();
        pocket.setBalance(balance);
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
        PocketDTO pocketDTO = pocketMapper.toDto(pocket);
        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
            .andExpect(status().isCreated());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeCreate + 1);
        Pocket testPocket = pocketList.get(pocketList.size() - 1);
        assertThat(testPocket.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testPocket.getLabel()).isEqualTo(DEFAULT_LABEL);
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
        PocketDTO pocketDTO = pocketMapper.toDto(pocket);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
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
        PocketDTO pocketDTO = pocketMapper.toDto(pocket);

        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
            .andExpect(status().isBadRequest());

        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLabelIsRequired() throws Exception {
        int databaseSizeBeforeTest = pocketRepository.findAll().size();
        // set the field null
        pocket.setLabel(null);

        // Create the Pocket, which fails.
        PocketDTO pocketDTO = pocketMapper.toDto(pocket);

        restPocketMockMvc.perform(post("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
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
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL.toString())))
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
            .andExpect(jsonPath("$.label").value(DEFAULT_LABEL.toString()))
            .andExpect(jsonPath("$.startDateTime").value(DEFAULT_START_DATE_TIME.toString()))
            .andExpect(jsonPath("$.endDateTime").value(DEFAULT_END_DATE_TIME.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.reserved").value(DEFAULT_RESERVED.intValue()));
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where key equals to DEFAULT_KEY
        defaultPocketShouldBeFound("key.equals=" + DEFAULT_KEY);

        // Get all the pocketList where key equals to UPDATED_KEY
        defaultPocketShouldNotBeFound("key.equals=" + UPDATED_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByKeyIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where key in DEFAULT_KEY or UPDATED_KEY
        defaultPocketShouldBeFound("key.in=" + DEFAULT_KEY + "," + UPDATED_KEY);

        // Get all the pocketList where key equals to UPDATED_KEY
        defaultPocketShouldNotBeFound("key.in=" + UPDATED_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where key is not null
        defaultPocketShouldBeFound("key.specified=true");

        // Get all the pocketList where key is null
        defaultPocketShouldNotBeFound("key.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByLabelIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where label equals to DEFAULT_LABEL
        defaultPocketShouldBeFound("label.equals=" + DEFAULT_LABEL);

        // Get all the pocketList where label equals to UPDATED_LABEL
        defaultPocketShouldNotBeFound("label.equals=" + UPDATED_LABEL);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByLabelIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where label in DEFAULT_LABEL or UPDATED_LABEL
        defaultPocketShouldBeFound("label.in=" + DEFAULT_LABEL + "," + UPDATED_LABEL);

        // Get all the pocketList where label equals to UPDATED_LABEL
        defaultPocketShouldNotBeFound("label.in=" + UPDATED_LABEL);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByLabelIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where label is not null
        defaultPocketShouldBeFound("label.specified=true");

        // Get all the pocketList where label is null
        defaultPocketShouldNotBeFound("label.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByStartDateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where startDateTime equals to DEFAULT_START_DATE_TIME
        defaultPocketShouldBeFound("startDateTime.equals=" + DEFAULT_START_DATE_TIME);

        // Get all the pocketList where startDateTime equals to UPDATED_START_DATE_TIME
        defaultPocketShouldNotBeFound("startDateTime.equals=" + UPDATED_START_DATE_TIME);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByStartDateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where startDateTime in DEFAULT_START_DATE_TIME or UPDATED_START_DATE_TIME
        defaultPocketShouldBeFound("startDateTime.in=" + DEFAULT_START_DATE_TIME + "," + UPDATED_START_DATE_TIME);

        // Get all the pocketList where startDateTime equals to UPDATED_START_DATE_TIME
        defaultPocketShouldNotBeFound("startDateTime.in=" + UPDATED_START_DATE_TIME);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByStartDateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where startDateTime is not null
        defaultPocketShouldBeFound("startDateTime.specified=true");

        // Get all the pocketList where startDateTime is null
        defaultPocketShouldNotBeFound("startDateTime.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByEndDateTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where endDateTime equals to DEFAULT_END_DATE_TIME
        defaultPocketShouldBeFound("endDateTime.equals=" + DEFAULT_END_DATE_TIME);

        // Get all the pocketList where endDateTime equals to UPDATED_END_DATE_TIME
        defaultPocketShouldNotBeFound("endDateTime.equals=" + UPDATED_END_DATE_TIME);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByEndDateTimeIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where endDateTime in DEFAULT_END_DATE_TIME or UPDATED_END_DATE_TIME
        defaultPocketShouldBeFound("endDateTime.in=" + DEFAULT_END_DATE_TIME + "," + UPDATED_END_DATE_TIME);

        // Get all the pocketList where endDateTime equals to UPDATED_END_DATE_TIME
        defaultPocketShouldNotBeFound("endDateTime.in=" + UPDATED_END_DATE_TIME);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByEndDateTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where endDateTime is not null
        defaultPocketShouldBeFound("endDateTime.specified=true");

        // Get all the pocketList where endDateTime is null
        defaultPocketShouldNotBeFound("endDateTime.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where amount equals to DEFAULT_AMOUNT
        defaultPocketShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the pocketList where amount equals to UPDATED_AMOUNT
        defaultPocketShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultPocketShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the pocketList where amount equals to UPDATED_AMOUNT
        defaultPocketShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where amount is not null
        defaultPocketShouldBeFound("amount.specified=true");

        // Get all the pocketList where amount is null
        defaultPocketShouldNotBeFound("amount.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByReservedIsEqualToSomething() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where reserved equals to DEFAULT_RESERVED
        defaultPocketShouldBeFound("reserved.equals=" + DEFAULT_RESERVED);

        // Get all the pocketList where reserved equals to UPDATED_RESERVED
        defaultPocketShouldNotBeFound("reserved.equals=" + UPDATED_RESERVED);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByReservedIsInShouldWork() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where reserved in DEFAULT_RESERVED or UPDATED_RESERVED
        defaultPocketShouldBeFound("reserved.in=" + DEFAULT_RESERVED + "," + UPDATED_RESERVED);

        // Get all the pocketList where reserved equals to UPDATED_RESERVED
        defaultPocketShouldNotBeFound("reserved.in=" + UPDATED_RESERVED);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByReservedIsNullOrNotNull() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);

        // Get all the pocketList where reserved is not null
        defaultPocketShouldBeFound("reserved.specified=true");

        // Get all the pocketList where reserved is null
        defaultPocketShouldNotBeFound("reserved.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllPocketsByBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        Balance balance = BalanceResourceIntTest.createEntity(em);
        em.persist(balance);
        em.flush();
        pocket.setBalance(balance);
        pocketRepository.saveAndFlush(pocket);
        Long balanceId = balance.getId();

        // Get all the pocketList where balance equals to balanceId
        defaultPocketShouldBeFound("balanceId.equals=" + balanceId);

        // Get all the pocketList where balance equals to balanceId + 1
        defaultPocketShouldNotBeFound("balanceId.equals=" + (balanceId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultPocketShouldBeFound(String filter) throws Exception {
        restPocketMockMvc.perform(get("/api/pockets?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pocket.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL.toString())))
            .andExpect(jsonPath("$.[*].startDateTime").value(hasItem(DEFAULT_START_DATE_TIME.toString())))
            .andExpect(jsonPath("$.[*].endDateTime").value(hasItem(DEFAULT_END_DATE_TIME.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].reserved").value(hasItem(DEFAULT_RESERVED.intValue())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultPocketShouldNotBeFound(String filter) throws Exception {
        restPocketMockMvc.perform(get("/api/pockets?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
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
        pocketRepository.saveAndFlush(pocket);
        int databaseSizeBeforeUpdate = pocketRepository.findAll().size();

        // Update the pocket
        Pocket updatedPocket = pocketRepository.findById(pocket.getId())
            .orElseThrow(() -> new IllegalStateException("Pocket not found for id " + pocket.getId()));
        // Disconnect from session so that the updates on updatedPocket are not directly saved in db
        em.detach(updatedPocket);
        updatedPocket
            .key(UPDATED_KEY)
            .label(UPDATED_LABEL)
            .startDateTime(UPDATED_START_DATE_TIME)
            .endDateTime(UPDATED_END_DATE_TIME)
            .amount(UPDATED_AMOUNT)
            .reserved(UPDATED_RESERVED);
        PocketDTO pocketDTO = pocketMapper.toDto(updatedPocket);

        log.info("{}", pocketDTO);

        restPocketMockMvc.perform(put("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
            .andExpect(status().isOk());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeUpdate);
        Pocket testPocket = pocketList.get(pocketList.size() - 1);
        assertThat(testPocket.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testPocket.getLabel()).isEqualTo(UPDATED_LABEL);
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
        PocketDTO pocketDTO = pocketMapper.toDto(pocket);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPocketMockMvc.perform(put("/api/pockets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(pocketDTO)))
            .andExpect(status().isCreated());

        // Validate the Pocket in the database
        List<Pocket> pocketList = pocketRepository.findAll();
        assertThat(pocketList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePocket() throws Exception {
        // Initialize the database
        pocketRepository.saveAndFlush(pocket);
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

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PocketDTO.class);
        PocketDTO pocketDTO1 = new PocketDTO();
        pocketDTO1.setId(1L);
        PocketDTO pocketDTO2 = new PocketDTO();
        assertThat(pocketDTO1).isNotEqualTo(pocketDTO2);
        pocketDTO2.setId(pocketDTO1.getId());
        assertThat(pocketDTO1).isEqualTo(pocketDTO2);
        pocketDTO2.setId(2L);
        assertThat(pocketDTO1).isNotEqualTo(pocketDTO2);
        pocketDTO1.setId(null);
        assertThat(pocketDTO1).isNotEqualTo(pocketDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(pocketMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(pocketMapper.fromId(null)).isNull();
    }
}
