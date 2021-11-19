package com.icthh.xm.ms.balance.repository;

import static java.sql.Timestamp.valueOf;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.icthh.xm.ms.balance.domain.Pocket;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Slf4j
public class PocketRepositoryIntTest extends BaseDaoTest {

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void findByLabelAndStartDateTimeAndEndDateTimeAndBalance() throws Exception {

        log.info("{}", pocketRepository.findAll());

        shouldExistsPocket("LABEL", "2018-05-01 00:00:00",
            "3019-05-01 00:00:00", 1L);
        shouldExistsPocket("LABEL", null, null, 3L);
        shouldExistsPocket("LABEL", "2018-05-01 00:00:01", null, 1L);
        shouldExistsPocket("LABEL", null, "2016-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", null, "2016-05-01 00:00:00", 2L);
        shouldNotExistsPocket("LABEL1", "2018-05-01 00:00:00",
            "3019-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", null, "2116-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", "2118-05-01 00:00:01", null, 1L);

        shouldExistsPocket("LABEL_28", null, null, 5L, "{\"value\":true}");
        shouldNotExistsPocket("LABEL_28", null, null, 5L);
    }


    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByDateLimit() {
        List<Long> expectedOrder = asList(11L, 18L, 24L, 13L, 22L, 25L, 23L, 14L, 15L, 16L, 19L, 17L, 20L, 21L);

        Page<Pocket> pockets = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(4L).get(),
            Instant.now(), PageRequest.of(0, 5));
        log.info("{}", pockets.getContent());
        assertEquals(expectedOrder.subList(0, 5), pockets.map(Pocket::getId).getContent());

        Page<Pocket> pockets1 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(4L).get(),
            Instant.now(), PageRequest.of(1, 5));
        log.info("{}", pockets1.getContent());
        assertEquals(expectedOrder.subList(5, 10), pockets1.map(Pocket::getId).getContent());

        Page<Pocket> pockets2 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(4L).get(),
            Instant.now(), PageRequest.of(2, 5));
        log.info("{}", pockets2.getContent());
        assertEquals(expectedOrder.subList(10, 14), pockets2.map(Pocket::getId).getContent());
    }

    private void shouldExistsPocket(String label, String startDateTime, String endDateTime, Long balanceId) {
        assertTrue(pocketRepository.findPocketForReload(label,
            startDateTime == null ? null : valueOf(startDateTime).toInstant(),
            endDateTime == null ? null : valueOf(endDateTime).toInstant(),
            balanceRepository.getOne(balanceId),
            null)
            .isPresent());
    }

    private void shouldExistsPocket(String label, String startDateTime, String endDateTime, Long balanceId, String metadata) {
        assertTrue(pocketRepository.findPocketForReload(label,
                                                        startDateTime == null ? null : valueOf(startDateTime).toInstant(),
                                                        endDateTime == null ? null : valueOf(endDateTime).toInstant(),
                                                        balanceRepository.getOne(balanceId),
                                                        metadata)
                                   .isPresent());
    }

    private void shouldNotExistsPocket(String label, String startDateTime, String endDateTime, Long balanceId) {
        assertFalse(pocketRepository.findPocketForReload(label,
            startDateTime == null ? null : valueOf(startDateTime).toInstant(),
            endDateTime == null ? null : valueOf(endDateTime).toInstant(),
            balanceRepository.getOne(balanceId),
            null)
            .isPresent());
    }

    @Test
    @DataSet(value = "mockBalancesWithZeroPockets-init.xml", disableConstraints = true)
    @ExpectedDataSet("mockBalancesWithZeroPockets-assert.xml")
    public void deleteZeroAmountInSpecifyBalance() {
        pocketRepository.deletePocketWithZeroAmount(1L);
    }


    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByStartDate() {

        Page<Pocket> pockets = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(6L).get(),
            Instant.parse("2013-05-01T00:00:00Z"), PageRequest.of(0, 5));
        log.info("{}", pockets.getContent());
        assertEquals(asList(31L, 32L), pockets.map(Pocket::getId).getContent());

        Page<Pocket> pockets1 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(6L).get(),
            Instant.parse("2015-05-01T00:00:00Z"), PageRequest.of(0, 5));
        log.info("{}", pockets1.getContent());
        assertEquals(asList(33L), pockets1.map(Pocket::getId).getContent());

        Page<Pocket> pockets2 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findById(6L).get(),
            Instant.parse("2011-05-01T00:00:00Z"), PageRequest.of(0, 5));
        log.info("{}", pockets2.getContent());
        assertEquals(asList(30L), pockets2.map(Pocket::getId).getContent());
    }
}

