package com.icthh.xm.ms.balance.service;

import static com.icthh.xm.ms.balance.service.OperationType.CHARGING;
import static com.icthh.xm.ms.balance.service.OperationType.RELOAD;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_FROM;
import static com.icthh.xm.ms.balance.service.OperationType.TRANSFER_TO;
import static com.icthh.xm.ms.balance.utils.TestReflectionUtils.setClock;
import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.balance.config.ApplicationProperties;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.BalanceSpec;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.repository.BalanceChangeEventRepository;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.PocketRepository;
import com.icthh.xm.ms.balance.service.dto.TransferDto;
import com.icthh.xm.ms.balance.service.mapper.BalanceChangeEventMapper;
import com.icthh.xm.ms.balance.service.mapper.BalanceChangeEventMapperImpl;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
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
    private MetricService metricService;
    @Mock
    private XmAuthenticationContextHolder authContextHolder;
    @Mock
    private BalanceChangeEventRepository balanceChangeEventRepository;
    @Mock
    private BalanceSpecService balanceSpecService;
    @Spy
    private BalanceChangeEventMapper balanceChangeEventMapper = new BalanceChangeEventMapperImpl();

    @Captor
    private ArgumentCaptor<BalanceChangeEvent> captor;

    private void expectedAuth() {
        XmAuthenticationContext auth = mock(XmAuthenticationContext.class);
        when(auth.getRequiredUserKey()).thenReturn("requiredUserKey");
        when(authContextHolder.getContext()).thenReturn(auth);
    }

    private void deleteZeroPocketDisabled() {
        when(balanceSpecService.getBalanceSpec(any())).thenReturn(new BalanceSpec.BalanceTypeSpec());
    }

    private void deleteZeroPocketEnabled() {
        BalanceSpec.BalanceTypeSpec spec = new BalanceSpec.BalanceTypeSpec();
        spec.setRemoveZeroPockets(true);
        when(balanceSpecService.getBalanceSpec(any())).thenReturn(spec);
    }

    @Test
    public void ifPocketExistsPocketReloaded() {
        expectedAuth();

        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        Pocket pocket = new Pocket().key("ASSERTION_KEY").label("label").amount(new BigDecimal("30"));
        pocket.setId(5L);

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label", ofEpochSecond(1525428386), null, balance))
        .thenReturn(of(pocket));
        when(pocketRepository.findOneByIdForUpdate(5L)).thenReturn(of(pocket));

        Pocket assertionPocket = new Pocket().key("ASSERTION_KEY").label("label").amount(new BigDecimal("80"));
        assertionPocket.setId(5L);
        when(pocketRepository.save(refEq(assertionPocket))).thenReturn(assertionPocket);

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));


        verify(pocketRepository).save(refEq(assertionPocket));
        verify(metricService).updateMaxMetric(balance);

        expectBalanceChangeEvents(createBalanceEvent("50", 1L, RELOAD, createPocketEvent("50", 5l, "ASSERTION_KEY", "label")));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    private PocketChangeEvent createPocketEvent(String amountDelta, Long pocketId, String assertionKey, String label) {
        return PocketChangeEvent.builder()
            .amountDelta(new BigDecimal(amountDelta))
            .pocketId(pocketId)
            .pocketKey(assertionKey)
            .pocketLabel(label)
            .build();
    }

    private void expectBalanceChangeEvents(BalanceChangeEvent... balanceChangeEvents) {
        String operationId = null;
        verify(balanceChangeEventRepository, times(balanceChangeEvents.length)).save(captor.capture());
        List<BalanceChangeEvent> allValues = captor.getAllValues();
        assertEquals(allValues.size(), balanceChangeEvents.length);
        for (int i = 0; i < allValues.size(); i++) {
            BalanceChangeEvent actual = allValues.get(i);
            BalanceChangeEvent expected = balanceChangeEvents[i];
            assertThat(actual, new ReflectionEquals(expected, "pocketChangeEvents", "operationId"));
            expectedPocketChangeEvents(actual, expected);

            if (operationId != null) {
                assertEquals(operationId, actual.getOperationId());
            }
            operationId = actual.getOperationId();
        }
    }

    private void expectedPocketChangeEvents(BalanceChangeEvent actual, BalanceChangeEvent expected) {
        List<PocketChangeEvent> expectedPockets = expected.getPocketChangeEvents();
        List<PocketChangeEvent> actualPockets = actual.getPocketChangeEvents();
        expectPocketChangeEvents(expectedPockets.size(), actualPockets.size());
        for (int i = 0; i < expectedPockets.size(); i++) {
            if (expectedPockets.get(i).getPocketKey() == null) {
                assertThat(actualPockets.get(i), new ReflectionEquals(expectedPockets.get(i), "transaction", "pocketKey"));
            } else {
                assertThat(actualPockets.get(i), new ReflectionEquals(expectedPockets.get(i), "transaction"));
            }
        }
    }

    private void expectPocketChangeEvents(int size, int size2) {
        assertEquals(size, size2);
    }

    private BalanceChangeEvent createBalanceEvent(String amountDelta, long balanceId, OperationType operationType, PocketChangeEvent... pocketChangeEvents) {
        return BalanceChangeEvent.builder()
                .amountDelta(new BigDecimal(amountDelta))
                .balanceId(balanceId)
                .executedByUserKey("requiredUserKey")
                .operationType(operationType)
                .operationDate(ofEpochSecond(1525428386))
                .pocketChangeEvents(asList(pocketChangeEvents))
                .build();
    }

    @Test
    public void ifPocketNotExistsNewPocketCreated() {
        expectedAuth();

        Balance balance = new Balance();
        balance.setId(1L);
        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label", ofEpochSecond(1525428386), null, balance))
            .thenReturn(empty());

        Pocket assertionPocket = new Pocket().label("label").startDateTime(ofEpochSecond(1525428386))
            .amount(new BigDecimal("50")).balance(balance);
        Pocket savedPocket = new Pocket().label("label").startDateTime(ofEpochSecond(1525428386))
            .amount(new BigDecimal("50")).balance(balance);
        savedPocket.setId(492L);
        when(pocketRepository.save(refEq(assertionPocket, "key"))).thenReturn(savedPocket);

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));

        verify(pocketRepository).save(refEq(assertionPocket, "key"));
        verify(metricService).updateMaxMetric(balance);

        expectBalanceChangeEvents(createBalanceEvent("50", 1L, RELOAD, createPocketEvent("50", 492L, null, "label")));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void ifBalanceNotFound() {
        when(balanceRepository.findOneByIdForUpdate(5L)).thenReturn(Optional.empty());
        balanceService.reload(new ReloadBalanceRequest().setBalanceId(5L));
        verifyNoMoreInteractions(balanceChangeEventRepository);
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
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successCheckoutAllManyFromOnePocket() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, new PageRequest(0, 100)))
            .thenReturn(pockets);
        setClock(balanceService, 1525428386000L);
        when(pocketRepository.save(refEq(pocket("98.78", "label1")))).thenReturn(pocket("98.78", "label1", 441L));

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setBalanceId(1L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(balance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verifyNoMoreInteractions(pocketRepository);
        verifyNoMoreInteractions(metricService);

        expectBalanceChangeEvents(createBalanceEvent("501.22", 1L, CHARGING, createPocketEvent("501.22", 441L, null, "label1")));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successChargingAllManyFromManyPocket() {
        expectedAuth();
        deleteZeroPocketEnabled();

        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.save(refEq(pocket("0", "label1")))).thenReturn(pocket("0", "label1", 11L));
        when(pocketRepository.save(refEq(pocket("0", "label2")))).thenReturn(pocket("0", "label2", 12L));
        when(pocketRepository.save(refEq(pocket("0", "label3")))).thenReturn(pocket("0", "label3", 13L));
        when(pocketRepository.save(refEq(pocket("0", "label4")))).thenReturn(pocket("0", "label4", 14L));
        when(pocketRepository.save(refEq(pocket("9", "label5")))).thenReturn(pocket("9", "label5", 15L));

        when(pocketRepository.findPocketForChargingOrderByDates(balance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, new PageRequest(1, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("100", "label4"),
                pocket("10", "label5"),
                pocket("15", "label6")
            )));

        setClock(balanceService, 1525428386000L);

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setBalanceId(1L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(balance, new PageRequest(0, 3));
        verify(pocketRepository).findPocketForChargingOrderByDates(balance, new PageRequest(1, 3));
        verify(pocketRepository).save(refEq(pocket("0", "label1")));
        verify(pocketRepository).save(refEq(pocket("0", "label2")));
        verify(pocketRepository).save(refEq(pocket("0", "label3")));
        verify(pocketRepository).save(refEq(pocket("0", "label4")));
        verify(pocketRepository).save(refEq(pocket("9", "label5")));
        verify(pocketRepository).deletePocketWithZeroAmount(eq(1L));
        verifyNoMoreInteractions(pocketRepository);
        verifyNoMoreInteractions(metricService);

        expectBalanceChangeEvents(createBalanceEvent("201", 1L, CHARGING,
            createPocketEvent("50", 11L, null, "label1"),
            createPocketEvent("30", 12L, null, "label2"),
            createPocketEvent("20", 13L, null, "label3"),
            createPocketEvent("100", 14L, null, "label4"),
            createPocketEvent("1", 15L, null, "label5")
        ));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwNoManyIfPocketsDoesNotHaveEnoughMany() {
        expectedAuth();

        Balance balance = new Balance();
        balance.setId(1L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(balance));
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.findPocketForChargingOrderByDates(balance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, new PageRequest(1, 3)))
            .thenReturn(new PageImpl<>(emptyList()));

        Pocket any = any();
        when(pocketRepository.save(any)).then(in -> in.getArguments()[0]);

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setBalanceId(1L)
        );

        verifyNoMoreInteractions(balanceChangeEventRepository);
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

        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successTransferFromOnePocketToExistsPocketInTargetBalance() {
        expectedAuth();
        deleteZeroPocketEnabled();

        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));
        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        when(pocketRepository.save(refEq(pocket("98.78", "label1")))).thenReturn(pocket("98.78", "label1", 9875L));
        when(pocketRepository.save(refEq(pocket("751.22", "label1", 10L)))).thenReturn(pocket("751.22", "label1", 10L));

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 100)))
            .thenReturn(pockets);

        Pocket pocket = new Pocket();
        pocket.setId(10L);
        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance))
            .thenReturn(of(pocket));

        when(pocketRepository.findOneByIdForUpdate(10L)).thenReturn(of(pocket("250", "label1", 10L)));

        setClock(balanceService, 1525428386000L);

        BigDecimal amountDelta = new BigDecimal("501.22");
        Long balanceFrom = 1L;
        Long balanceTo = 2L;
        TransferDto transfer = balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(amountDelta)
                .setSourceBalanceId(balanceFrom)
                .setTargetBalanceId(balanceTo)
        );

        assertEquals(amountDelta, transfer.getFrom().getAmountDelta());
        assertEquals(amountDelta, transfer.getTo().getAmountDelta());
        assertEquals(balanceTo, transfer.getTo().getBalanceId());
        assertEquals(balanceFrom, transfer.getFrom().getBalanceId());
        assertEquals(TRANSFER_FROM, transfer.getFrom().getOperationType());
        assertEquals(TRANSFER_TO, transfer.getTo().getOperationType());

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance);
        verify(pocketRepository).findOneByIdForUpdate(10L);
        verify(pocketRepository).save(refEq(pocket("751.22", "label1", 10L)));
        verify(pocketRepository).deletePocketWithZeroAmount(eq(1L));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("501.22", 1L, TRANSFER_FROM, createPocketEvent("501.22", 9875L, null, "label1")),
            createBalanceEvent("501.22", 2L, TRANSFER_TO, createPocketEvent("501.22", 10L, null, "label1"))
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successTransferFromOnePocketToNewPocketInTargetBalance() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));
        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        when(pocketRepository.save(refEq(pocket("98.78", "label1"), "key", "balance"))).thenReturn(pocket("98.78", "label1", 17L));
        when(pocketRepository.save(refEq(pocket("501.22", "label1"), "key", "balance"))).thenReturn(pocket("501.22", "label1", 18L));

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1"),
            pocket("300", "label2")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 100)))
            .thenReturn(pockets);

        when(pocketRepository.findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance))
            .thenReturn(empty());

        setClock(balanceService, 1525428386000L);

        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1"), "key", "balance"));
        verify(pocketRepository).findByLabelAndStartDateTimeAndEndDateTimeAndBalance("label1", null, null, targetBalance);
        verify(pocketRepository).save(refEq(pocket("501.22", "label1"), "key", "balance"));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("501.22", 1L, TRANSFER_FROM, createPocketEvent("501.22", 17L, null, "label1")),
            createBalanceEvent("501.22", 2L, TRANSFER_TO, createPocketEvent("501.22", 18L, null, "label1"))
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successTransferFromManyPocket() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance sourceBalance = new Balance();
        sourceBalance.setId(1L);
        Balance targetBalance = new Balance();
        targetBalance.setId(2L);

        when(balanceRepository.findOneByIdForUpdate(1L)).thenReturn(of(sourceBalance));
        when(balanceRepository.findOneByIdForUpdate(2L)).thenReturn(of(targetBalance));
        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal("600")));
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, new PageRequest(1, 3)))
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
        setClock(balanceService, 1525428386000L);



        when(pocketRepository.save(refEq(pocket("0", "label1")))).thenReturn(pocket("0", "label1", 78L));
        when(pocketRepository.save(refEq(pocket("0", "label2")))).thenReturn(pocket("0", "label2", 79L));
        when(pocketRepository.save(refEq(pocket("0", "label3")))).thenReturn(pocket("0", "label3", 80L));
        when(pocketRepository.save(refEq(pocket("0", "label4")))).thenReturn(pocket("0", "label4", 81L));
        when(pocketRepository.save(refEq(pocket("9", "label5")))).thenReturn(pocket("9", "label5", 82L));


        when(pocketRepository.save(refEq(pocket("50", "label1"), "key", "balance"))).thenReturn(pocket("50", "label1", 83L));
        when(pocketRepository.save(refEq(pocket("40", "label2", 10L), "key", "balance"))).thenReturn(pocket("40", "label2", 10L));
        when(pocketRepository.save(refEq(pocket("35", "label3", 12L), "key", "balance"))).thenReturn(pocket("35", "label3", 12L));
        when(pocketRepository.save(refEq(pocket("100", "label4"), "key", "balance"))).thenReturn(pocket("100", "label4", 84L));
        when(pocketRepository.save(refEq(pocket("1", "label5"), "key", "balance"))).thenReturn(pocket("1", "label5", 85L));



        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, new PageRequest(0, 3));
        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, new PageRequest(1, 3));
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

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("201", 1L, TRANSFER_FROM,
                createPocketEvent("50", 78L, null, "label1"),
                createPocketEvent("30", 79L, null, "label2"),
                createPocketEvent("20", 80L, null, "label3"),
                createPocketEvent("100", 81L, null, "label4"),
                createPocketEvent("1", 82L, null, "label5")
            ),
            createBalanceEvent("201", 2L, TRANSFER_TO,
                createPocketEvent("50", 83L, null, "label1"),
                createPocketEvent("30", 10L, null, "label2"),
                createPocketEvent("20", 12L, null, "label3"),
                createPocketEvent("100", 84L, null, "label4"),
                createPocketEvent("1", 85L, null, "label5")
            )
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

}
