package com.icthh.xm.ms.balance.service;

import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class BalanceServiceUnitTest {

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private PocketRepository pocketRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private MetricRepository metricRepository;

    @Test
    public void ifPocketExistsPocketReloaded() {

        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().balance(balance).typeKey("MAX").value("300")));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("250")));
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

        verify(metricRepository).findByTypeKeyAndBalance("MAX", balance);
        verifyNoMoreInteractions(metricRepository);
    }

    @Test
    public void ifPocketNotExistsNewPocketCreated() {
        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().balance(balance).typeKey("MAX").value("100")));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("250")));

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label", ofEpochSecond(1525428386), null, balance))
            .thenReturn(empty());

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));

        Pocket assertionPocket = new Pocket().label("label").startDateTime(ofEpochSecond(1525428386))
            .amount(new BigDecimal("50")).balance(balance);

        verify(pocketRepository).save(refEq(assertionPocket, "key"));
        verify(metricRepository).findByTypeKeyAndBalance("MAX", balance);
        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("250"), "key", "balance"));
    }

    @Test(expected = EntityNotFoundException.class)
    public void ifBalanceNotFound() {
        when(balanceRepository.findOneByIdForUpdate(5L)).thenReturn(Optional.empty());
        balanceService.reload(new ReloadBalanceRequest().setBalanceId(5L));
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwExceptionIfNoManyDuringCheckout() {
        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("19.73")));

        balanceService.charging(new ChargingBalanceRequest()
            .setAmount(new BigDecimal("20"))
            .setBalanceId(1L)
        );
    }

    @Test
    public void successCheckoutAllManyFromOnePocket() {
        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForCheckoutOrderByDates(balance, new PageRequest(0, 100)))
            .thenReturn(pockets);

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setBalanceId(1L)
        );

        verify(pocketRepository).findPocketForCheckoutOrderByDates(balance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verifyNoMoreInteractions(pocketRepository);
    }

    @Test
    public void successCheckoutAllManyFromManyPocket() {
        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));

        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.findPocketForCheckoutOrderByDates(balance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForCheckoutOrderByDates(balance, new PageRequest(1, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("100", "label4"),
                pocket("10", "label5"),
                pocket("15", "label6")
            )));

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setBalanceId(1L)
        );

        verify(pocketRepository).findPocketForCheckoutOrderByDates(balance, new PageRequest(0, 3));
        verify(pocketRepository).findPocketForCheckoutOrderByDates(balance, new PageRequest(1, 3));
        verify(pocketRepository).save(refEq(pocket("0", "label1")));
        verify(pocketRepository).save(refEq(pocket("0", "label2")));
        verify(pocketRepository).save(refEq(pocket("0", "label3")));
        verify(pocketRepository).save(refEq(pocket("0", "label4")));
        verify(pocketRepository).save(refEq(pocket("9", "label5")));
        verifyNoMoreInteractions(pocketRepository);
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwNoManyIfPocketsDoesNotHaveEnoughMany() {
        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.findPocketForCheckoutOrderByDates(balance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForCheckoutOrderByDates(balance, new PageRequest(1, 3)))
            .thenReturn(new PageImpl<>(emptyList()));

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setBalanceId(1L)
        );
    }

    private Pocket pocket(String amount, String label) {
        return new Pocket().amount(new BigDecimal(amount)).label(label);
    }

    private Pocket pocket(String amount, String label, Long id) {
        Pocket pocket = new Pocket().amount(new BigDecimal(amount)).label(label);
        pocket.setId(id);
        return pocket;
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwExceptionIfNoManyDuringTransfer() {
        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("19.73")));

        balanceService.transfer(new TransferBalanceRequest()
            .setAmount(new BigDecimal("20"))
            .setSourceBalanceId(1L)
        );
    }

    @Test
    public void successTransferFromOnePocketToExistsPocketInTargetBalance() {
        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));

        when(metricRepository.findByTypeKeyAndBalance("MAX", targetBalance)).thenReturn(empty());
        when(balanceRepository.findBalanceAmount(targetBalance)).thenReturn(of(new BigDecimal("400")));

        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 100)))
            .thenReturn(pockets);

        Pocket pocket = new Pocket();
        pocket.setId(10L);
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance))
            .thenReturn(of(pocket));

        when(pocketRepository.findOneByIdForUpdate(10L)).thenReturn(of(pocket("250", "label1", 10L)));

        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance);
        verify(pocketRepository).findOneByIdForUpdate(10L);
        verify(pocketRepository).save(refEq(pocket("751.22", "label1", 10L)));

        verify(metricRepository).findByTypeKeyAndBalance("MAX", targetBalance);
        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("400"), "key", "balance"));

        verifyNoMoreInteractions(pocketRepository);
    }

    @Test
    public void successTransferFromOnePocketToNewPocketInTargetBalance() {
        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));

        when(metricRepository.findByTypeKeyAndBalance("MAX", targetBalance)).thenReturn(empty());
        when(balanceRepository.findBalanceAmount(targetBalance)).thenReturn(of(new BigDecimal("300")));

        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 100)))
            .thenReturn(pockets);

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance))
            .thenReturn(empty());

        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1"), "key", "balance"));
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance);
        verify(pocketRepository).save(refEq(pocket("501.22", "label1"), "key", "balance"));

        verify(metricRepository).findByTypeKeyAndBalance("MAX", targetBalance);
        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("300"), "key", "balance"));

        verifyNoMoreInteractions(pocketRepository);
    }

    @Test
    public void successTransferFromManyPocket() {
        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));
        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));

        when(metricRepository.findByTypeKeyAndBalance("MAX", targetBalance)).thenReturn(empty());
        when(balanceRepository.findBalanceAmount(targetBalance)).thenReturn(of(new BigDecimal("200")));

        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);
        when(pocketRepository.findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(1, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("100", "label4"),
                pocket("10", "label5"),
                pocket("15", "label6")
            )));

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance))
            .thenReturn(empty());
        Pocket pocket = pocket("10", "label2", 10L);
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label2", null, null, targetBalance))
            .thenReturn(of(pocket));
        when(pocketRepository.findOneByIdForUpdate(10L)).thenReturn(of(pocket));
        Pocket pocket2 = pocket("15", "label3", 12L);
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label3", null, null, targetBalance))
            .thenReturn(of(pocket2));
        when(pocketRepository.findOneByIdForUpdate(12L)).thenReturn(of(pocket2));
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label4", null, null, targetBalance))
            .thenReturn(empty());
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label5", null, null, targetBalance))
            .thenReturn(empty());

        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(0, 3));
        verify(pocketRepository).findPocketForCheckoutOrderByDates(sourceBalance, new PageRequest(1, 3));
        verify(pocketRepository).save(refEq(pocket("0", "label1")));
        verify(pocketRepository).save(refEq(pocket("0", "label2")));
        verify(pocketRepository).save(refEq(pocket("0", "label3")));
        verify(pocketRepository).save(refEq(pocket("0", "label4")));
        verify(pocketRepository).save(refEq(pocket("9", "label5")));


        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance);
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label2", null, null, targetBalance);
        verify(pocketRepository).findOneByIdForUpdate(10L);
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label3", null, null, targetBalance);
        verify(pocketRepository).findOneByIdForUpdate(12L);
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label4", null, null, targetBalance);
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label5", null, null, targetBalance);

        verify(pocketRepository).save(refEq(pocket("50", "label1"), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("40", "label2", 10L), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("35", "label3", 12L), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("100", "label4"), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("1", "label5"), "key", "balance"));

        verify(metricRepository).findByTypeKeyAndBalance("MAX", targetBalance);
        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("200"), "key", "balance"));

        verifyNoMoreInteractions(pocketRepository);
    }


}
