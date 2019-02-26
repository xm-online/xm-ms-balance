package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.service.MetricQueryService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;
import com.icthh.xm.ms.balance.service.mapper.MetricMapper;
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
 * Test class for the MetricResource REST controller.
 *
 * @see MetricResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BalanceApp.class, SecurityBeanOverrideConfiguration.class})
public class MetricResourceIntTest {

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE_KEY = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private MetricMapper metricMapper;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricQueryService metricQueryService;

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

    private MockMvc restMetricMockMvc;

    private Metric metric;

    @BeforeTransaction
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final MetricResource metricResource = new MetricResource(metricService, metricQueryService);
        this.restMetricMockMvc = MockMvcBuilders.standaloneSetup(metricResource)
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
    public static Metric createEntity(EntityManager em) {
        Metric metric = new Metric()
            .key(DEFAULT_KEY)
            .typeKey(DEFAULT_TYPE_KEY)
            .value(DEFAULT_VALUE);
        // Add required entity
        Balance balance = BalanceResourceIntTest.createEntity(em);
        em.persist(balance);
        em.flush();
        metric.setBalance(balance);
        return metric;
    }

    @Before
    public void initTest() {
        metric = createEntity(em);
    }

    @Test
    @Transactional
    public void createMetric() throws Exception {
        int databaseSizeBeforeCreate = metricRepository.findAll().size();

        // Create the Metric
        MetricDTO metricDTO = metricMapper.toDto(metric);
        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isCreated());

        // Validate the Metric in the database
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeCreate + 1);
        Metric testMetric = metricList.get(metricList.size() - 1);
        assertThat(testMetric.getKey()).isEqualTo(DEFAULT_KEY);
        assertThat(testMetric.getTypeKey()).isEqualTo(DEFAULT_TYPE_KEY);
        assertThat(testMetric.getValue()).isEqualTo(DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void createMetricWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = metricRepository.findAll().size();

        // Create the Metric with an existing ID
        metric.setId(1L);
        MetricDTO metricDTO = metricMapper.toDto(metric);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Metric in the database
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = metricRepository.findAll().size();
        // set the field null
        metric.setKey(null);

        // Create the Metric, which fails.
        MetricDTO metricDTO = metricMapper.toDto(metric);

        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isBadRequest());

        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = metricRepository.findAll().size();
        // set the field null
        metric.setTypeKey(null);

        // Create the Metric, which fails.
        MetricDTO metricDTO = metricMapper.toDto(metric);

        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isBadRequest());

        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetrics() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList
        restMetricMockMvc.perform(get("/api/metrics?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metric.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));
    }

    @Test
    @Transactional
    public void getMetric() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get the metric
        restMetricMockMvc.perform(get("/api/metrics/{id}", metric.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(metric.getId().intValue()))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY.toString()))
            .andExpect(jsonPath("$.typeKey").value(DEFAULT_TYPE_KEY.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()));
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where key equals to DEFAULT_KEY
        defaultMetricShouldBeFound("key.equals=" + DEFAULT_KEY);

        // Get all the metricList where key equals to UPDATED_KEY
        defaultMetricShouldNotBeFound("key.equals=" + UPDATED_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByKeyIsInShouldWork() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where key in DEFAULT_KEY or UPDATED_KEY
        defaultMetricShouldBeFound("key.in=" + DEFAULT_KEY + "," + UPDATED_KEY);

        // Get all the metricList where key equals to UPDATED_KEY
        defaultMetricShouldNotBeFound("key.in=" + UPDATED_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where key is not null
        defaultMetricShouldBeFound("key.specified=true");

        // Get all the metricList where key is null
        defaultMetricShouldNotBeFound("key.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByTypeKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where typeKey equals to DEFAULT_TYPE_KEY
        defaultMetricShouldBeFound("typeKey.equals=" + DEFAULT_TYPE_KEY);

        // Get all the metricList where typeKey equals to UPDATED_TYPE_KEY
        defaultMetricShouldNotBeFound("typeKey.equals=" + UPDATED_TYPE_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByTypeKeyIsInShouldWork() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where typeKey in DEFAULT_TYPE_KEY or UPDATED_TYPE_KEY
        defaultMetricShouldBeFound("typeKey.in=" + DEFAULT_TYPE_KEY + "," + UPDATED_TYPE_KEY);

        // Get all the metricList where typeKey equals to UPDATED_TYPE_KEY
        defaultMetricShouldNotBeFound("typeKey.in=" + UPDATED_TYPE_KEY);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByTypeKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where typeKey is not null
        defaultMetricShouldBeFound("typeKey.specified=true");

        // Get all the metricList where typeKey is null
        defaultMetricShouldNotBeFound("typeKey.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByValueIsEqualToSomething() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where value equals to DEFAULT_VALUE
        defaultMetricShouldBeFound("value.equals=" + DEFAULT_VALUE);

        // Get all the metricList where value equals to UPDATED_VALUE
        defaultMetricShouldNotBeFound("value.equals=" + UPDATED_VALUE);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByValueIsInShouldWork() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where value in DEFAULT_VALUE or UPDATED_VALUE
        defaultMetricShouldBeFound("value.in=" + DEFAULT_VALUE + "," + UPDATED_VALUE);

        // Get all the metricList where value equals to UPDATED_VALUE
        defaultMetricShouldNotBeFound("value.in=" + UPDATED_VALUE);
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);

        // Get all the metricList where value is not null
        defaultMetricShouldBeFound("value.specified=true");

        // Get all the metricList where value is null
        defaultMetricShouldNotBeFound("value.specified=false");
    }

    @Test
    @WithMockUser(authorities = "SUPER-ADMIN")
    @Transactional
    public void getAllMetricsByBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        Balance balance = BalanceResourceIntTest.createEntity(em);
        em.persist(balance);
        em.flush();
        metric.setBalance(balance);
        metricRepository.saveAndFlush(metric);
        Long balanceId = balance.getId();

        // Get all the metricList where balance equals to balanceId
        defaultMetricShouldBeFound("balanceId.equals=" + balanceId);

        // Get all the metricList where balance equals to balanceId + 1
        defaultMetricShouldNotBeFound("balanceId.equals=" + (balanceId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultMetricShouldBeFound(String filter) throws Exception {
        restMetricMockMvc.perform(get("/api/metrics?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metric.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY.toString())))
            .andExpect(jsonPath("$.[*].typeKey").value(hasItem(DEFAULT_TYPE_KEY.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultMetricShouldNotBeFound(String filter) throws Exception {
        restMetricMockMvc.perform(get("/api/metrics?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getNonExistingMetric() throws Exception {
        // Get the metric
        restMetricMockMvc.perform(get("/api/metrics/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMetric() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);
        int databaseSizeBeforeUpdate = metricRepository.findAll().size();

        // Update the metric
        Metric updatedMetric = metricRepository.findById(metric.getId())
            .orElseThrow(() -> new IllegalStateException("Metric not found for id " + metric.getId()));
        // Disconnect from session so that the updates on updatedMetric are not directly saved in db
        em.detach(updatedMetric);
        updatedMetric
            .key(UPDATED_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .value(UPDATED_VALUE);
        MetricDTO metricDTO = metricMapper.toDto(updatedMetric);

        restMetricMockMvc.perform(put("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isOk());

        // Validate the Metric in the database
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeUpdate);
        Metric testMetric = metricList.get(metricList.size() - 1);
        assertThat(testMetric.getKey()).isEqualTo(UPDATED_KEY);
        assertThat(testMetric.getTypeKey()).isEqualTo(UPDATED_TYPE_KEY);
        assertThat(testMetric.getValue()).isEqualTo(UPDATED_VALUE);
    }

    @Test
    @Transactional
    public void updateNonExistingMetric() throws Exception {
        int databaseSizeBeforeUpdate = metricRepository.findAll().size();

        // Create the Metric
        MetricDTO metricDTO = metricMapper.toDto(metric);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMetricMockMvc.perform(put("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metricDTO)))
            .andExpect(status().isCreated());

        // Validate the Metric in the database
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMetric() throws Exception {
        // Initialize the database
        metricRepository.saveAndFlush(metric);
        int databaseSizeBeforeDelete = metricRepository.findAll().size();

        // Get the metric
        restMetricMockMvc.perform(delete("/api/metrics/{id}", metric.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Metric.class);
        Metric metric1 = new Metric();
        metric1.setId(1L);
        Metric metric2 = new Metric();
        metric2.setId(metric1.getId());
        assertThat(metric1).isEqualTo(metric2);
        metric2.setId(2L);
        assertThat(metric1).isNotEqualTo(metric2);
        metric1.setId(null);
        assertThat(metric1).isNotEqualTo(metric2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetricDTO.class);
        MetricDTO metricDTO1 = new MetricDTO();
        metricDTO1.setId(1L);
        MetricDTO metricDTO2 = new MetricDTO();
        assertThat(metricDTO1).isNotEqualTo(metricDTO2);
        metricDTO2.setId(metricDTO1.getId());
        assertThat(metricDTO1).isEqualTo(metricDTO2);
        metricDTO2.setId(2L);
        assertThat(metricDTO1).isNotEqualTo(metricDTO2);
        metricDTO1.setId(null);
        assertThat(metricDTO1).isNotEqualTo(metricDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(metricMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(metricMapper.fromId(null)).isNull();
    }
}
