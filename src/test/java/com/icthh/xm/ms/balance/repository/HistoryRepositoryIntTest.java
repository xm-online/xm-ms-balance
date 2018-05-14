package com.icthh.xm.ms.balance.repository;

import static com.icthh.xm.ms.balance.service.OperationType.RELOAD;
import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.database.rider.core.api.dataset.DataSet;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
public class HistoryRepositoryIntTest extends BaseDaoTest {

    @Autowired
    private BalanceChangeEventRepository balanceChangeEventRepository;

    @Autowired
    private PocketChangeEventRepository pocketChangeEventRepository;

    @Test
    @DataSet(value = "mockHistory-init.xml", disableConstraints = true)
    public void testSearchPocketsByHistoryRequest() {
        Page<PocketChangeEvent> pocketsHidtory = pocketChangeEventRepository.findByEntityIdInAndOperationTypeAndOperationDateBetween(
            asList(2L, 4L, 5L),
            RELOAD,
            ofEpochSecond(1525132800L),
            ofEpochSecond(1527811200L),
            new PageRequest(0, 50)
        );

        List<PocketChangeEvent> content = pocketsHidtory.getContent();
        assertEquals(content.size(), 10);
        Set<Long> events = content.stream().map(PocketChangeEvent::getId).collect(toSet());
        assertTrue(events.contains(31L));
        assertTrue(events.contains(71L));
        assertTrue(events.contains(91L));
        assertTrue(events.contains(131L));
        assertTrue(events.contains(171L));
        assertTrue(events.contains(32L));
        assertTrue(events.contains(72L));
        assertTrue(events.contains(92L));
        assertTrue(events.contains(132L));
        assertTrue(events.contains(172L));
        log.info("{}", content);
    }

    @Test
    @DataSet(value = "mockHistory-init.xml", disableConstraints = true)
    public void testSearchBalancesByHistoryRequest() {
        Page<BalanceChangeEvent> balancesHistory = balanceChangeEventRepository.findByEntityIdInAndOperationTypeAndOperationDateBetween(
            asList(2L, 4L, 5L),
            RELOAD,
            ofEpochSecond(1525132800L),
            ofEpochSecond(1527811200L),
            new PageRequest(0, 50)
        );

        List<BalanceChangeEvent> content = balancesHistory.getContent();
        assertEquals(content.size(), 5);
        Set<Long> events = content.stream().map(BalanceChangeEvent::getId).collect(toSet());
        assertTrue(events.contains(3L));
        assertTrue(events.contains(7L));
        assertTrue(events.contains(9L));
        assertTrue(events.contains(13L));
        assertTrue(events.contains(17L));
        log.info("{}", content);
    }

}
