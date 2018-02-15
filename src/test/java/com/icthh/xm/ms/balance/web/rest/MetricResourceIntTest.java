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

import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.service.MetricService;

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
    private MetricService metricService;

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
        final MetricResource metricResource = new MetricResource(metricService);
        this.restMetricMockMvc = MockMvcBuilders.standaloneSetup(metricResource)
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
        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metric)))
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

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metric)))
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

        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metric)))
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

        restMetricMockMvc.perform(post("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metric)))
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
        metricService.save(metric);

        int databaseSizeBeforeUpdate = metricRepository.findAll().size();

        // Update the metric
        Metric updatedMetric = metricRepository.findOne(metric.getId());
        // Disconnect from session so that the updates on updatedMetric are not directly saved in db
        em.detach(updatedMetric);
        updatedMetric
            .key(UPDATED_KEY)
            .typeKey(UPDATED_TYPE_KEY)
            .value(UPDATED_VALUE);

        restMetricMockMvc.perform(put("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedMetric)))
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

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMetricMockMvc.perform(put("/api/metrics")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(metric)))
            .andExpect(status().isCreated());

        // Validate the Metric in the database
        List<Metric> metricList = metricRepository.findAll();
        assertThat(metricList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMetric() throws Exception {
        // Initialize the database
        metricService.save(metric);

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
}
