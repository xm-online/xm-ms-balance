package com.icthh.xm.ms.balance.service.lep;

import com.icthh.xm.commons.lep.spring.LepSpringConfiguration;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.mapper.BalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestLepConfiguration.class})
@ActiveProfiles("test")
@DirtiesContext
@ContextConfiguration(classes = {BalanceService.class})
public class BalanceLepIntTest {

    @Autowired
    private BalanceService balanceService;

    @MockBean
    private BalanceRepository balanceRepository;
    @MockBean
    private PermittedRepository permittedRepository;
    @MockBean
    private PocketRepository pocketRepository;
    @MockBean
    private BalanceMapper balanceMapper;
    @MockBean
    private ApplicationProperties applicationProperties;
    @MockBean
    private MetricService metricService;

    @Test
    public void test() {
        log.info("{}", balanceService.getClass());
    }

}
