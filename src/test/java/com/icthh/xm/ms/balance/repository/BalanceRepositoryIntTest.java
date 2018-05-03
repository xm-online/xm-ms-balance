package com.icthh.xm.ms.balance.repository;

import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.ms.balance.domain.Balance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class BalanceRepositoryIntTest extends BaseDaoTest {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PocketRepository pocketRepository;

    @Test
    @DataSet(value = "amountCalculatedFromPockedWithFilterByDate-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByDate() {
        Optional<BigDecimal> balanceAmount = balanceRepository.getBalanceAmount(balanceRepository.findOne(1L), ofEpochSecond(1525350677));
        assertEquals(new BigDecimal("123.00"), balanceAmount.get());
        log.info("{}", balanceAmount);
    }

    @Test
    @DataSet(value = "amountCalculatedFromPockedWithFilterByDate-init.xml", disableConstraints = true)
    public void returnAmountsByBalancesWithFilterByDate() {
        List<Balance> balances = asList(balanceRepository.findOne(1L), balanceRepository.findOne(2L));
        List<BalanceAmountDto> balancesAmount = balanceRepository.getBalancesAmount(balances, ofEpochSecond(1525350677));
        assertEquals(balancesAmount.get(0).getAmount(), new BigDecimal("123.00"));
        assertEquals(balancesAmount.get(1).getAmount(), new BigDecimal("10000.00"));
        assertEquals(balancesAmount.size(), 2);
    }

}
