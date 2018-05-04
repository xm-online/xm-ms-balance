package com.icthh.xm.ms.balance.repository;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.ms.balance.domain.Balance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class BalanceRepositoryIntTest extends BaseDaoTest {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PocketRepository pocketRepository;

    @Test
    @DataSet(value = "amountCalculatedFromPockedWithFilterByDate-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByDate() {
        Optional<BigDecimal> balanceAmount = balanceRepository.getBalanceAmount(balanceRepository.findOne(1L));
        assertEquals(new BigDecimal("123.00"), balanceAmount.get());
        log.info("{}", balanceAmount);
    }

    @Test
    @DataSet(value = "amountCalculatedFromPockedWithFilterByDate-init.xml", disableConstraints = true)
    public void returnAmountsByBalancesWithFilterByDate() {
        List<Balance> balances = asList(balanceRepository.findOne(1L), balanceRepository.findOne(2L));
        log.info("{}", balances);
        Map<Long, BigDecimal> balancesAmount = balanceRepository.getBalancesAmountMap(balances);
        log.info("{}", balancesAmount);
        assertEquals(2, balancesAmount.size());
        assertEquals(new BigDecimal("123.00"), balancesAmount.get(1L));
        assertEquals(new BigDecimal("10000.00"), balancesAmount.get(2L));
    }

}
