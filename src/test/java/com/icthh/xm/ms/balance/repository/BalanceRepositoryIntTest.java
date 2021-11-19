package com.icthh.xm.ms.balance.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.ms.balance.domain.Balance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@Slf4j
public class BalanceRepositoryIntTest extends BaseDaoTest {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PocketRepository pocketRepository;

    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByDate() {
        Optional<BigDecimal> balanceAmount = balanceRepository.findBalanceAmount(
            balanceRepository.findById(1L).get(), Instant.now());
        assertEquals(new BigDecimal("123.00"), balanceAmount.get());
        log.info("{}", balanceAmount);

        balanceAmount = balanceRepository.findBalanceAmount(
            balanceRepository.findById(1L).get(), Instant.parse("2013-05-01T00:00:06Z"));
        assertEquals(new BigDecimal("700.00"), balanceAmount.get());
        balanceAmount = balanceRepository.findBalanceAmount(
            balanceRepository.findById(1L).get(), Instant.parse("2015-05-03T00:00:06Z"));
        assertEquals(new BigDecimal("1400.00"), balanceAmount.get());
    }

    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void returnAmountsByBalancesWithFilterByDate() {
        List<Balance> balances = asList(balanceRepository.findById(1L).get(),
            balanceRepository.findById(2L).get());
        log.info("{}", balances);
        Map<Long, BigDecimal> balancesAmount = balanceRepository.getBalancesAmountMap(balances);
        log.info("{}", balancesAmount);
        assertEquals(2, balancesAmount.size());
        assertEquals(new BigDecimal("123.00"), balancesAmount.get(1L));
        assertEquals(new BigDecimal("10000.00"), balancesAmount.get(2L));
    }
}
