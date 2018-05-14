package com.icthh.xm.ms.balance.repository;

import static java.sql.Timestamp.valueOf;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.ms.balance.domain.Pocket;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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

        shouldExistsPocket("LABEL", "2018-05-01 00:00:00", "3019-05-01 00:00:00", 1L);
        shouldExistsPocket("LABEL", null, null, 3L);
        shouldExistsPocket("LABEL", "2018-05-01 00:00:01", null, 1L);
        shouldExistsPocket("LABEL", null, "2016-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", null, "2016-05-01 00:00:00", 2L);
        shouldNotExistsPocket("LABEL1", "2018-05-01 00:00:00", "3019-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", null, "2116-05-01 00:00:00", 1L);
        shouldNotExistsPocket("LABEL", "2118-05-01 00:00:01", null, 1L);
    }


    @Test
    @DataSet(value = "mockBalances-init.xml", disableConstraints = true)
    public void amountCalculatedFromPockedWithFilterByDateLimit() {
        List<Long> expectedOrder = asList(11L, 18L, 24L, 13L, 22L, 25L, 23L, 14L, 15L, 16L, 19L, 17L, 20L, 21L);

        Page<Pocket> pockets = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findOne(4L),
            new PageRequest(0, 5));
        log.info("{}",pockets.getContent());
        assertEquals(expectedOrder.subList(0, 5), pockets.map(Pocket::getId).getContent());

        Page<Pocket> pockets1 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findOne(4L),
            new PageRequest(1, 5));
        log.info("{}",pockets1.getContent());
        assertEquals(expectedOrder.subList(5, 10), pockets1.map(Pocket::getId).getContent());

        Page<Pocket> pockets2 = pocketRepository.findPocketForChargingOrderByDates(balanceRepository.findOne(4L),
            new PageRequest(2, 5));
        log.info("{}",pockets2.getContent());
        assertEquals(expectedOrder.subList(10, 14), pockets2.map(Pocket::getId).getContent());
    }

    private void shouldExistsPocket(String label, String startDateTime, String endDateTime, Long balanceId) {
        assertTrue(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance(label,
            startDateTime == null ? null : valueOf(startDateTime).toInstant(),
            endDateTime == null ? null : valueOf(endDateTime).toInstant(),
            balanceRepository.findOne(balanceId))
            .isPresent());
    }

    private void shouldNotExistsPocket(String label, String startDateTime, String endDateTime, Long balanceId) {
        assertFalse(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance(label,
            startDateTime == null ? null : valueOf(startDateTime).toInstant(),
            endDateTime == null ? null : valueOf(endDateTime).toInstant(),
            balanceRepository.findOne(balanceId))
            .isPresent());
    }

}

