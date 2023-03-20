package com.icthh.xm.ms.balance.repository;

import static com.google.common.collect.ImmutableMap.of;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepScriptConstants.BINDING_KEY_AUTH_CONTEXT;
import static com.icthh.xm.ms.balance.config.jsonb.JsonbUtils.jsonIntField;
import static com.icthh.xm.ms.balance.config.jsonb.JsonbUtils.jsonTextField;
import static com.icthh.xm.ms.balance.domain.Metadata_.JSON;
import static com.icthh.xm.ms.balance.domain.Pocket_.metadata;
import static org.junit.Assert.assertEquals;

import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metadata;
import com.icthh.xm.ms.balance.domain.Pocket;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.Expression;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, BalanceApp.class})
@ContextConfiguration(initializers = {com.icthh.xm.ms.balance.repository.PostgressConfigIntTest.Initializer.class})
@ActiveProfiles("pg-test")
public class PostgressConfigIntTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
        .withDatabaseName("balance")
        .withUsername("sa")
        .withPassword("sa");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                "spring.datasource.password=" + postgreSQLContainer.getPassword()
                                 ).applyTo(configurableApplicationContext.getEnvironment());
            log.info("spring.datasource.url: {}", postgreSQLContainer.getJdbcUrl());
        }
    }

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmAuthenticationContextHolder xmAuthenticationContextHolder;

    @Before
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "public");
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

    @Test
    public void testSearchUsingMetadata() {

        Balance balance = balanceRepository.save(new Balance().typeKey("BALANCE").key("8").entityId(8L));
        Pocket falsePocket = pocketRepository.save(pocket(18L, balance, of("bvalue", "false")));
        Pocket truePocket = pocketRepository.save(pocket(28L, balance, of("bvalue", "true")));
        Pocket first3Pocket = pocketRepository.save(pocket(38L, balance, of("eId", "3")));
        Pocket second3Pocket = pocketRepository.save(pocket(48L, balance, of("eId", "3")));
        pocketRepository.save(pocket(58L, balance, of("eId", "4")));
        pocketRepository.save(pocket(68L, balance, of("eId", "4")));

        List<Pocket> pockets3 = pocketRepository.findAll(Specification.where(
            (Specification<Pocket>) (root, query, cb) -> {
                Expression<Integer> eId = jsonIntField(cb, root.get(metadata).get(JSON), "eId");
                return cb.equal(eId, 3);
            }));

        List<Pocket> falsePockets = pocketRepository.findAll(Specification.where(
            (Specification<Pocket>) (root, query, cb) -> {
                Expression<String> bvalue = jsonTextField(cb, root.get(metadata).get(JSON), "bvalue");
                return cb.equal(bvalue, "false");
            }));

        List<Pocket> truePockets = pocketRepository.findAll(Specification.where(
            (Specification<Pocket>) (root, query, cb) -> {
                Expression<String> bvalue = jsonTextField(cb, root.get(metadata).get(JSON), "bvalue");
                return cb.equal(bvalue, "true");
            }));

        assertEquals(2, pockets3.size());
        assertEquals(first3Pocket, pockets3.get(0));
        assertEquals(second3Pocket, pockets3.get(1));

        assertEquals(1, truePockets.size());
        assertEquals(truePocket, truePockets.get(0));

        assertEquals(1, falsePockets.size());
        assertEquals(falsePocket, falsePockets.get(0));
    }

    public Pocket pocket(Long key, Balance balance, Map<String, String> metadata) {
        return new Pocket().key(key.toString()).label("label_" + key)
                           .balance(balance)
                           .amount(new BigDecimal(key.toString()))
                           .metadata(new Metadata(metadata));
    }

}
