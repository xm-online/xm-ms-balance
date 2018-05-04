package com.icthh.xm.ms.balance.repository;

import static java.sql.Timestamp.valueOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.database.rider.core.api.dataset.DataSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

