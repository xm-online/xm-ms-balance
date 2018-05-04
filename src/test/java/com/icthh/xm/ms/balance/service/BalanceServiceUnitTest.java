package com.icthh.xm.ms.balance.service;

import static java.time.Instant.ofEpochSecond;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class BalanceServiceUnitTest {

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private PocketRepository pocketRepository;

    @Test
    public void ifPocketExistsPocketReloaded() {

        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        Pocket pocket = new Pocket().key("ASSERTION_KEY").amount(new BigDecimal("30"));
        pocket.setId(5L);

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label", ofEpochSecond(1525428386), null, balance))
        .thenReturn(of(pocket));
        when(pocketRepository.findOneByIdForUpdate(5L)).thenReturn(of(pocket));

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));


        Pocket assertionPocket = new Pocket().key("ASSERTION_KEY").amount(new BigDecimal("80"));
        assertionPocket.setId(5L);
        verify(pocketRepository).save(refEq(assertionPocket));
    }

    @Test
    public void ifPocketNotExistsNewPocketCreated() {
        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label", ofEpochSecond(1525428386), null, balance))
            .thenReturn(empty());

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));

        Pocket assertionPocket = new Pocket().label("label").startDateTime(ofEpochSecond(1525428386))
            .amount(new BigDecimal("50")).balance(balance);

        verify(pocketRepository).save(refEq(assertionPocket));
    }

    @Test(expected = EntityNotFoundException.class)
    public void ifBalanceNotFound() {
        when(balanceRepository.findOneByIdForUpdate(5L)).thenReturn(Optional.empty());
        balanceService.reload(new ReloadBalanceRequest().setBalanceId(5L));
    }

}
