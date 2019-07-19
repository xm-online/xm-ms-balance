package com.icthh.xm.ms.balance.service;

import static com.google.common.collect.ImmutableMap.of;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
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
import com.icthh.xm.ms.balance.domain.Metadata;
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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
public class BalanceServiceUnitTest {

    public static final String EMPTY_METADATA_VALUE = null;

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

    private long pocketId = 0;

    @Before
    public void before() {
        balanceService.setSelf(balanceService);
    }

    private void expectedAuth() {
        XmAuthenticationContext auth = mock(XmAuthenticationContext.class);
        when(auth.getRequiredUserKey()).thenReturn("requiredUserKey");
        when(authContextHolder.getContext()).thenReturn(auth);
    }

    private void deleteZeroPocketDisabled() {
        when(balanceSpecService.getBalanceSpec(Matchers.any())).thenReturn(new BalanceSpec.BalanceTypeSpec());
    }

    private void deleteZeroPocketEnabled() {
        BalanceSpec.BalanceTypeSpec spec = new BalanceSpec.BalanceTypeSpec();
        spec.setRemoveZeroPockets(true);
        when(balanceSpecService.getBalanceSpec(Matchers.any())).thenReturn(spec);
    }

    @Test
    public void ifPocketExistsPocketReloaded() {
        expectedAuth();

        Balance balance = createBalanceWithAmount(1L, "30");
        Pocket pocket = new Pocket().key("ASSERTION_KEY").label("label").amount(new BigDecimal("30"));
        pocket.setId(5L);

        when(pocketRepository.findPocketForReload("label", ofEpochSecond(1525428386), null, balance,
                                                  EMPTY_METADATA_VALUE))
            .thenReturn(of(pocket));
        when(pocketRepository.findOneByIdForUpdate(5L)).thenReturn(of(pocket));

        Pocket assertionPocket = new Pocket().key("ASSERTION_KEY").label("label").amount(new BigDecimal("80"));
        assertionPocket.setId(5L);
        when(pocketRepository.save(refEq(assertionPocket))).thenReturn(assertionPocket);

        balanceService.reload(new ReloadBalanceRequest().setBalanceId(1L).setAmount(new BigDecimal("50"))
            .setStartDateTime(ofEpochSecond(1525428386)).setLabel("label"));


        verify(pocketRepository).save(refEq(assertionPocket));
        verify(metricService).updateMaxMetric(balance);

        expectBalanceChangeEvents(createBalanceEvent("50", 1L, RELOAD, "80", "30",
            createPocketEvent("50", 5l, "ASSERTION_KEY", "label", "80", "30")));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    private PocketChangeEvent createPocketEvent(String amountDelta, Long pocketId, String assertionKey, String label,
                                                String amountAfter, String amountBefore) {
        return PocketChangeEvent.builder()
            .amountDelta(new BigDecimal(amountDelta))
            .pocketId(pocketId)
            .pocketKey(assertionKey)
            .pocketLabel(label)
            .metadata(new Metadata())
            .amountAfter(new BigDecimal(amountAfter))
            .amountBefore(new BigDecimal(amountBefore))
            .build();
    }

    private PocketChangeEvent createPocketEvent(String amountDelta, Long pocketId, String assertionKey, String label,
                                                Map<String, String> metadata, String amountAfter, String amountBefore) {
        return PocketChangeEvent.builder()
            .amountDelta(new BigDecimal(amountDelta))
            .pocketId(pocketId)
            .pocketKey(assertionKey)
            .pocketLabel(label)
            .metadata(new Metadata(metadata))
            .amountAfter(new BigDecimal(amountAfter))
            .amountBefore(new BigDecimal(amountBefore))
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
            assertTrue(new ReflectionEquals(expected,
                "pocketChangeEvents", "operationId").matches(actual));
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
                assertTrue(new ReflectionEquals(expectedPockets.get(i), "transaction", "pocketKey")
                    .matches(actualPockets.get(i)));
            } else {
                assertTrue(new ReflectionEquals(expectedPockets.get(i), "transaction")
                    .matches(actualPockets.get(i)));
            }
        }
    }

    private void expectPocketChangeEvents(int size, int size2) {
        assertEquals(size, size2);
    }

    private BalanceChangeEvent createBalanceEvent(String amountDelta,
                                                  long balanceId,
                                                  OperationType operationType,
                                                  Map<String, String> metadata,
                                                  String amountAfter,
                                                  String amountBefore,
                                                  PocketChangeEvent... pocketChangeEvents) {
        return BalanceChangeEvent.builder()
            .amountDelta(new BigDecimal(amountDelta))
            .balanceId(balanceId)
            .executedByUserKey("requiredUserKey")
            .operationType(operationType)
            .operationDate(ofEpochSecond(1525428386))
            .pocketChangeEvents(asList(pocketChangeEvents))
            .metadata(new Metadata(metadata))
            .amountAfter(new BigDecimal(amountAfter))
            .amountBefore(new BigDecimal(amountBefore))
            .build();
    }

    private BalanceChangeEvent createBalanceEvent(String amountDelta,
                                                  long balanceId,
                                                  OperationType operationType,
                                                  String amountAfter,
                                                  String amountBefore,
                                                  PocketChangeEvent... pocketChangeEvents) {
        return BalanceChangeEvent.builder()
            .amountDelta(new BigDecimal(amountDelta))
            .balanceId(balanceId)
            .executedByUserKey("requiredUserKey")
            .operationType(operationType)
            .operationDate(ofEpochSecond(1525428386))
            .pocketChangeEvents(asList(pocketChangeEvents))
            .metadata(new Metadata())
            .amountAfter(new BigDecimal(amountAfter))
            .amountBefore(new BigDecimal(amountBefore))
            .build();
    }

    @Test
    public void ifPocketNotExistsNewPocketCreated() {
        expectedAuth();

        Balance balance = createBalanceWithAmount(1L, "0");
        when(pocketRepository.findPocketForReload("label", ofEpochSecond(1525428386), null, balance,
                                                  EMPTY_METADATA_VALUE)).thenReturn(empty());

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

        expectBalanceChangeEvents(createBalanceEvent("50", 1L, RELOAD, "50", "0",
            createPocketEvent("50", 492L, null, "label", "50", "0")));
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
        Balance balance = createBalance(1L);
        expectBalance(balance, "19.73");

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

        Balance balance = createBalanceWithAmount(1L, "600");
        expectBalance(balance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, PageRequest.of(0, 100)))
            .thenReturn(pockets);
        setClock(balanceService, 1525428386000L);
        when(pocketRepository.save(refEq(pocket("98.78", "label1"))))
            .thenReturn(pocket("98.78", "label1", 441L));

        balanceService.charging(
            new ChargingBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setBalanceId(1L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(balance, PageRequest.of(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verifyNoMoreInteractions(pocketRepository);
        verifyNoMoreInteractions(metricService);

        expectBalanceChangeEvents(createBalanceEvent("501.22", 1L, CHARGING, "98.78", "600",
            createPocketEvent("501.22", 441L, null, "label1", "98.78", "600")));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successChargingAllManyFromManyPocket() {
        expectedAuth();
        deleteZeroPocketEnabled();

        Balance balance = createBalanceWithAmount(1L, "600");
        expectBalance(balance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.save(refEq(pocket("0", "label1"))))
            .thenReturn(pocket("0", "label1", 11L));
        when(pocketRepository.save(refEq(pocket("0", "label2"))))
            .thenReturn(pocket("0", "label2", 12L));
        when(pocketRepository.save(refEq(pocket("0", "label3"))))
            .thenReturn(pocket("0", "label3", 13L));
        when(pocketRepository.save(refEq(pocket("0", "label4"))))
            .thenReturn(pocket("0", "label4", 14L));
        when(pocketRepository.save(refEq(pocket("9", "label5"))))
            .thenReturn(pocket("9", "label5", 15L));

        when(pocketRepository.findPocketForChargingOrderByDates(balance, PageRequest.of(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, PageRequest.of(1, 3)))
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

        verify(pocketRepository).findPocketForChargingOrderByDates(balance, PageRequest.of(0, 3));
        verify(pocketRepository).findPocketForChargingOrderByDates(balance, PageRequest.of(1, 3));
        verify(pocketRepository).save(refEq(pocket("0", "label1")));
        verify(pocketRepository).save(refEq(pocket("0", "label2")));
        verify(pocketRepository).save(refEq(pocket("0", "label3")));
        verify(pocketRepository).save(refEq(pocket("0", "label4")));
        verify(pocketRepository).save(refEq(pocket("9", "label5")));
        verify(pocketRepository).deletePocketWithZeroAmount(eq(1L));
        verifyNoMoreInteractions(pocketRepository);
        verifyNoMoreInteractions(metricService);

        expectBalanceChangeEvents(createBalanceEvent("201", 1L, CHARGING, "399", "600",
            createPocketEvent("50", 11L, null, "label1", "0", "50"),
            createPocketEvent("30", 12L, null, "label2", "0", "30"),
            createPocketEvent("20", 13L, null, "label3", "0", "20"),
            createPocketEvent("100", 14L, null, "label4", "0", "100"),
            createPocketEvent("1", 15L, null, "label5", "9", "10")
        ));
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwNoManyIfPocketsDoesNotHaveEnoughMany() {
        expectedAuth();

        Balance balance = createBalanceWithAmount(1L, "600");
        expectBalance(balance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);

        when(pocketRepository.findPocketForChargingOrderByDates(balance, PageRequest.of(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(balance, PageRequest.of(1, 3)))
            .thenReturn(new PageImpl<>(emptyList()));

        Pocket any = Matchers.any();
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

    private Pocket pocket(String amount, String label, Map<String, String> data) {
        return new Pocket().amount(new BigDecimal(amount)).label(label).metadata(new Metadata(data));
    }

    private Pocket pocket(String amount, String label, Long id) {
        Pocket pocket = new Pocket().amount(new BigDecimal(amount)).label(label);
        pocket.setId(id);
        return pocket;
    }

    private Pocket pocket(String amount, String label, Long id, Map<String, String> metadata) {
        Pocket pocket = new Pocket().amount(new BigDecimal(amount)).label(label);
        pocket.setId(id);
        pocket.metadata(new Metadata(metadata));
        return pocket;
    }

    @Test(expected = NoEnoughMoneyException.class)
    public void throwExceptionIfNoManyDuringTransfer() {
        Balance balance = createBalance(1L);
        expectBalance(balance, "19.73");

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

        Balance sourceBalance = createBalanceWithAmount(1L, "600");
        Balance targetBalance = createBalanceWithAmount(2L, "250");

        expectBalance(sourceBalance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        when(pocketRepository.save(refEq(pocket("98.78", "label1"))))
            .thenReturn(pocket("98.78", "label1", 9875L));
        when(pocketRepository.save(refEq(pocket("751.22", "label1", 10L))))
            .thenReturn(pocket("751.22", "label1", 10L));

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100)))
            .thenReturn(pockets);

        Pocket pocket = new Pocket();
        pocket.setId(10L);
        when(pocketRepository.findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE))
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

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1")));
        verify(pocketRepository).findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).findOneByIdForUpdate(10L);
        verify(pocketRepository).save(refEq(pocket("751.22", "label1", 10L)));
        verify(pocketRepository).deletePocketWithZeroAmount(eq(1L));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("501.22", 1L, TRANSFER_FROM,"98.78", "600",
                createPocketEvent("501.22", 9875L, null, "label1", "98.78", "600")),
            createBalanceEvent("501.22", 2L, TRANSFER_TO, "751.22", "250",
                createPocketEvent("501.22", 10L, null, "label1", "751.22", "250"))
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successTransferFromOnePocketToNewPocketInTargetBalance() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance sourceBalance = createBalanceWithAmount(1L, "600");
        Balance targetBalance = createBalanceWithAmount(2L, "0");

        expectBalance(sourceBalance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);
        when(pocketRepository.save(refEq(pocket("98.78", "label1"), "key", "balance")))
            .thenReturn(pocket("98.78", "label1", 17L));
        when(pocketRepository.save(refEq(pocket("501.22", "label1"), "key", "balance")))
            .thenReturn(pocket("501.22", "label1", 18L));

        Page<Pocket> pockets = new PageImpl<>(asList(
            pocket("600", "label1")
        ));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100)))
            .thenReturn(pockets);

        when(pocketRepository.findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(empty());

        setClock(balanceService, 1525428386000L);

        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("501.22"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100));
        verify(pocketRepository).save(refEq(pocket("98.78", "label1"), "key", "balance"));
        verify(pocketRepository).findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).save(refEq(pocket("501.22", "label1"), "key", "balance"));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("501.22", 1L, TRANSFER_FROM, "98.78", "600",
                createPocketEvent("501.22", 17L, null, "label1", "98.78", "600")),
            createBalanceEvent("501.22", 2L, TRANSFER_TO, "501.22", "0",
                createPocketEvent("501.22", 18L, null, "label1", "501.22", "0"))
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void successTransferFromManyPocket() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance sourceBalance = createBalanceWithAmount(1L, "600");
        Balance targetBalance = createBalanceWithAmount(2L, "25");

        expectBalance(sourceBalance, "600");
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(3);
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("50", "label1"),
                pocket("30", "label2"),
                pocket("20", "label3")
            )));
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(1, 3)))
            .thenReturn(new PageImpl<>(asList(
                pocket("100", "label4"),
                pocket("10", "label5"),
                pocket("15", "label6")
            )));

        when(pocketRepository.findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(empty());
        Pocket pocket = pocket("10", "label2", 10L);
        when(pocketRepository.findPocketForReload("label2", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(of(pocket));
        when(pocketRepository.findOneByIdForUpdate(10L)).thenReturn(of(pocket));
        Pocket pocket2 = pocket("15", "label3", 12L);
        when(pocketRepository.findPocketForReload("label3", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(of(pocket2));
        when(pocketRepository.findOneByIdForUpdate(12L)).thenReturn(of(pocket2));
        when(pocketRepository.findPocketForReload("label4", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(empty());
        when(pocketRepository.findPocketForReload("label5", null, null, targetBalance, EMPTY_METADATA_VALUE))
            .thenReturn(empty());
        setClock(balanceService, 1525428386000L);


        when(pocketRepository.save(refEq(pocket("0", "label1"))))
            .thenReturn(pocket("0", "label1", 78L));
        when(pocketRepository.save(refEq(pocket("0", "label2"))))
            .thenReturn(pocket("0", "label2", 79L));
        when(pocketRepository.save(refEq(pocket("0", "label3"))))
            .thenReturn(pocket("0", "label3", 80L));
        when(pocketRepository.save(refEq(pocket("0", "label4"))))
            .thenReturn(pocket("0", "label4", 81L));
        when(pocketRepository.save(refEq(pocket("9", "label5"))))
            .thenReturn(pocket("9", "label5", 82L));


        when(pocketRepository.save(refEq(pocket("50", "label1"), "key", "balance")))
            .thenReturn(pocket("50", "label1", 83L));
        when(pocketRepository.save(refEq(pocket("40", "label2", 10L), "key", "balance")))
            .thenReturn(pocket("40", "label2", 10L));
        when(pocketRepository.save(refEq(pocket("35", "label3", 12L), "key", "balance")))
            .thenReturn(pocket("35", "label3", 12L));
        when(pocketRepository.save(refEq(pocket("100", "label4"), "key", "balance")))
            .thenReturn(pocket("100", "label4", 84L));
        when(pocketRepository.save(refEq(pocket("1", "label5"), "key", "balance")))
            .thenReturn(pocket("1", "label5", 85L));


        balanceService.transfer(
            new TransferBalanceRequest()
                .setAmount(new BigDecimal("201"))
                .setSourceBalanceId(1L)
                .setTargetBalanceId(2L)
        );

        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 3));
        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(1, 3));
        verify(pocketRepository).save(refEq(pocket("0", "label1")));
        verify(pocketRepository).save(refEq(pocket("0", "label2")));
        verify(pocketRepository).save(refEq(pocket("0", "label3")));
        verify(pocketRepository).save(refEq(pocket("0", "label4")));
        verify(pocketRepository).save(refEq(pocket("9", "label5")));


        verify(pocketRepository).findPocketForReload("label1", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).findPocketForReload("label2", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).findOneByIdForUpdate(10L);
        verify(pocketRepository).findPocketForReload("label3", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).findOneByIdForUpdate(12L);
        verify(pocketRepository).findPocketForReload("label4", null, null, targetBalance, EMPTY_METADATA_VALUE);
        verify(pocketRepository).findPocketForReload("label5", null, null, targetBalance, EMPTY_METADATA_VALUE);

        verify(pocketRepository).save(refEq(pocket("50", "label1"), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("40", "label2", 10L), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("35", "label3", 12L), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("100", "label4"), "key", "balance"));
        verify(pocketRepository).save(refEq(pocket("1", "label5"), "key", "balance"));

        //verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("201", 1L, TRANSFER_FROM, "399", "600",
                createPocketEvent("50", 78L, null, "label1", "0", "50"),
                createPocketEvent("30", 79L, null, "label2", "0", "30"),
                createPocketEvent("20", 80L, null, "label3", "0", "20"),
                createPocketEvent("100", 81L, null, "label4", "0", "100"),
                createPocketEvent("1", 82L, null, "label5", "9", "10")
            ),
            createBalanceEvent("201", 2L, TRANSFER_TO, "226", "25",
                createPocketEvent("50", 83L, null, "label1", "50", "0"),
                createPocketEvent("30", 10L, null, "label2", "40", "10"),
                createPocketEvent("20", 12L, null, "label3", "35", "15"),
                createPocketEvent("100", 84L, null, "label4", "100", "0"),
                createPocketEvent("1", 85L, null, "label5", "1", "0")
            )
        );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void reloadWithMetadata() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance targetBalance = createBalanceWithAmount(1L, "10");

        Map<String, String> metadata = of("dataKey", "dataValue");
        Pocket pocket = pocket("10", "l1", 85L, metadata);
        expectPocketForReload(targetBalance, pocket, "l1", metadata, 85L);
        Pocket toSave = pocket("15", "l1", 85L, metadata);
        expectSavePocket(toSave, toSave);

        setClock(balanceService, 1525428386000L);

        balanceService.reload(reloadBalanceRequest(1L, "5", "l1", metadata));

        Pocket savedPocket = pocket("15", "l1", 85L, metadata);
        verifyFindPocketForReload(targetBalance, "l1", metadata, 85L);
        verifySavePocket(savedPocket);
        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);
        expectBalanceChangeEvents(
            createBalanceEvent("5", 1L, RELOAD, metadata, "15", "10",
                               createPocketEvent("5", 85L, null, "l1", metadata, "15", "10"))
                                 );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    @Test
    public void reloadWithMetadataCreateNewPocket() {
        expectedAuth();
        deleteZeroPocketDisabled();

        Balance targetBalance = createBalanceWithAmount(1L, "0");

        Map<String, String> metadata = of("dataKey", "dataValue");
        expectEmptyPocket(targetBalance, metadata, "l1");

        Pocket pocket = pocket("5", "l1", metadata);
        Pocket toSave = pocket("5", "l1", 85L, metadata);
        expectSavePocket(pocket, toSave);

        setClock(balanceService, 1525428386000L);

        balanceService.reload(reloadBalanceRequest(1L, "5", "l1", metadata));

        Pocket savedPocket = pocket("5", "l1", metadata);
        verifyFindPocketForReload(targetBalance, metadata, "l1");
        verifySavePocket(savedPocket);
        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);
        expectBalanceChangeEvents(
            createBalanceEvent("5", 1L, RELOAD, metadata, "5", "0",
                               createPocketEvent("5", 85L, null, "l1", metadata, "5", "0"))
                                 );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    private void verifyFindPocketForReload(Balance targetBalance, Map<String, String> metadata, String label) {
        verify(pocketRepository).findPocketForReload(label, null, null, targetBalance,
                                                     new Metadata(metadata).getValue());
    }

    private void expectEmptyPocket(Balance targetBalance, Map<String, String> metadata, String label) {
        when(pocketRepository.findPocketForReload(label, null, null, targetBalance,
                                                  new Metadata(metadata).getValue())).thenReturn(empty());
    }

    @Test
    public void testMergeMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fxRateCurrency", "USD");
        metadata.put("isResell", "true");
        metadata.put("pricePerCredit", "1");
        metadata.put("rate", "1.0");
        metadata.put("resellerAccountId", "18403");
        metadata.put("salesOrderNumber", "5");
        Metadata from = new Metadata();
        from.setValue(new Metadata(metadata).getValue());
        Metadata to = new Metadata();
        assertEquals(new Metadata(metadata).getValue(), this.balanceService.mergeMetadata(from, to).getValue());
    }

    @Test
    public void transferWithoutMetadataButPocketWithMetadata() {
        expectedAuth();
        deleteZeroPocketDisabled();
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);

        setClock(balanceService, 1525428386000L);

        Balance sourceBalance = createBalanceWithAmount(1L, "50");
        Balance targetBalance = createBalanceWithAmount(2L, "10");
        expectBalance(sourceBalance, "50");
        pocketForReload(sourceBalance, asList(
            pocket("10", "l1", 85L),
            pocket("10", "l1", 86L, of("dataKey", "dataValue")),
            pocket("10", "l1", 87L, of("dataKey", "dataValue2")),
            pocket("10", "l2", 88L),
            pocket("10", "l3", 89L, of("other", "value")))
                       );
        {
            Pocket pocket = pocket("5", "l1", 185L);
            expectPocketForReload(targetBalance, pocket, "l1", null, 185L);
            Pocket toSave = pocket("15", "l1", 185L);
            expectSavePocket(toSave, toSave);

            Pocket reloaded = pocket("0", "l1", 85L);
            expectSavePocket(reloaded, reloaded);
        }

        {
            Pocket pocket = pocket("5", "l1", 186L, of("dataKey", "dataValue"));
            expectPocketForReload(targetBalance, pocket, "l1", of("dataKey", "dataValue"), 186L);
            Pocket toSave = pocket("15", "l1", 186L, of("dataKey", "dataValue"));
            expectSavePocket(toSave, toSave);

            Pocket reloaded = pocket("0", "l1", 86L, of("dataKey", "dataValue"));
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, of("dataKey", "dataValue2"), "l1");
            Pocket pocket = pocket("10", "l1", of("dataKey", "dataValue2"));
            Pocket toSave = pocket("10", "l1", 187L, of("dataKey", "dataValue2"));
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("0", "l1", 87L, of("dataKey", "dataValue2"));
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, null, "l2");
            Pocket pocket = pocket("10", "l2");
            Pocket toSave = pocket("10", "l2", 188L);
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("0", "l2", 88L);
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, of("other", "value"), "l3");
            Pocket pocket = pocket("6", "l3", of("other", "value"));
            Pocket toSave = pocket("6", "l3", 189L, of("other", "value"));
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("4", "l3", 89L, of("other", "value"));
            expectSavePocket(reloaded, reloaded);
        }

        balanceService.transfer(new TransferBalanceRequest().setAmount(new BigDecimal("46")).setSourceBalanceId(1L)
                               .setTargetBalanceId(2L));

        verifyFindPocketForCharging(sourceBalance);

        verifyFindPocketForReload(targetBalance, "l1", null, 185L);
        verifySavePocket(pocket("0", "l1", 85L));
        verifySavePocket(pocket("15", "l1", 185L));

        verifyFindPocketForReload(targetBalance, "l1", of("dataKey", "dataValue"), 186L);
        verifySavePocket(pocket("0", "l1", 86L, of("dataKey", "dataValue")));
        verifySavePocket(pocket("15", "l1", 186L, of("dataKey", "dataValue")));


        verifyFindPocketForReload(targetBalance, of("dataKey", "dataValue2"), "l1");
        verifySavePocket(pocket("0", "l1", 87L, of("dataKey", "dataValue2")));
        verifySavePocket(pocket("10", "l1", of("dataKey", "dataValue2")));

        verifyFindPocketForReload(targetBalance, null, "l2");
        verifySavePocket(pocket("0", "l2", 88L));
        verifySavePocket(pocket("10", "l2"));


        verifyFindPocketForReload(targetBalance, of("other", "value"), "l3");
        verifySavePocket(pocket("4", "l3", 89L, of("other", "value")));
        verifySavePocket(pocket("6", "l3", of("other", "value")));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("46", 1L, TRANSFER_FROM, "4", "50",
                               createPocketEvent("10", 85L, null, "l1", null, "0", "10"),
                               createPocketEvent("10", 86L, null, "l1", of("dataKey", "dataValue"), "0", "10"),
                               createPocketEvent("10", 87L, null, "l1", of("dataKey", "dataValue2"), "0", "10"),
                               createPocketEvent("10", 88L, null, "l2", null, "0", "10"),
                               createPocketEvent("6", 89L, null, "l3", of("other", "value"), "4", "10")
                              ),
            createBalanceEvent("46", 2L, TRANSFER_TO, "56", "10",
                               createPocketEvent("10", 185L, null, "l1", null, "15", "5"),
                               createPocketEvent("10", 186L, null, "l1", of("dataKey", "dataValue"), "15", "5"),
                               createPocketEvent("10", 187L, null, "l1", of("dataKey", "dataValue2"), "10", "0"),
                               createPocketEvent("10", 188L, null, "l2", null, "10", "0"),
                               createPocketEvent("6", 189L, null, "l3", of("other", "value"), "6", "0")
                              )
                                 );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }


    @Test
    public void transferWithMetadata() {
        expectedAuth();
        deleteZeroPocketDisabled();
        when(applicationProperties.getPocketChargingBatchSize()).thenReturn(100);

        setClock(balanceService, 1525428386000L);

        Balance sourceBalance = createBalanceWithAmount(1L, "50");
        Balance targetBalance = createBalanceWithAmount(2L, "10");
        expectBalance(sourceBalance, "50");
        pocketForReload(sourceBalance, asList(
            pocket("10", "l1", 85L),
            pocket("10", "l1", 86L, of("dataKey", "dataValue")),
            pocket("10", "l1", 87L, of("dataKey", "dataValue2")),
            pocket("10", "l2", 88L),
            pocket("10", "l3", 89L, of("other", "value")))
                       );
        {
            Pocket pocket = pocket("5", "l1", 185L, of("transfer", "data"));
            expectPocketForReload(targetBalance, pocket, "l1", of("transfer", "data"), 185L);
            Pocket toSave = pocket("15", "l1", 185L, of("transfer", "data"));
            expectSavePocket(toSave, toSave);

            Pocket reloaded = pocket("0", "l1", 85L);
            expectSavePocket(reloaded, reloaded);
        }

        {
            Pocket pocket = pocket("5", "l1", 186L, of("dataKey", "dataValue", "transfer", "data"));
            expectPocketForReload(targetBalance, pocket, "l1", of("dataKey", "dataValue", "transfer", "data"), 186L);
            Pocket toSave = pocket("15", "l1", 186L, of("dataKey", "dataValue", "transfer", "data"));
            expectSavePocket(toSave, toSave);

            Pocket reloaded = pocket("0", "l1", 86L, of("dataKey", "dataValue"));
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, of("dataKey", "dataValue2", "transfer", "data"), "l1");
            Pocket pocket = pocket("10", "l1", of("dataKey", "dataValue2", "transfer", "data"));
            Pocket toSave = pocket("10", "l1", 187L, of("dataKey", "dataValue2", "transfer", "data"));
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("0", "l1", 87L, of("dataKey", "dataValue2"));
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, of("transfer", "data"), "l2");
            Pocket pocket = pocket("10", "l2", of("transfer", "data"));
            Pocket toSave = pocket("10", "l2", 188L, of("transfer", "data"));
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("0", "l2", 88L);
            expectSavePocket(reloaded, reloaded);
        }

        {
            expectEmptyPocket(targetBalance, of("other", "value", "transfer", "data"), "l3");
            Pocket pocket = pocket("6", "l3", of("other", "value", "transfer", "data"));
            Pocket toSave = pocket("6", "l3", 189L, of("other", "value", "transfer", "data"));
            expectSavePocket(pocket, toSave);

            Pocket reloaded = pocket("4", "l3", 89L, of("other", "value"));
            expectSavePocket(reloaded, reloaded);
        }

        balanceService.transfer(new TransferBalanceRequest().setAmount(new BigDecimal("46")).setSourceBalanceId(1L)
                                    .setTargetBalanceId(2L).setMetadata(of("transfer", "data")));

        verifyFindPocketForCharging(sourceBalance);

        verifyFindPocketForReload(targetBalance, "l1", of("transfer", "data"), 185L);
        verifySavePocket(pocket("0", "l1", 85L));
        verifySavePocket(pocket("15", "l1", 185L, of("transfer", "data")));

        verifyFindPocketForReload(targetBalance, "l1", of("dataKey", "dataValue", "transfer", "data"), 186L);
        verifySavePocket(pocket("0", "l1", 86L, of("dataKey", "dataValue")));
        verifySavePocket(pocket("15", "l1", 186L, of("dataKey", "dataValue", "transfer", "data")));


        verifyFindPocketForReload(targetBalance, of("dataKey", "dataValue2", "transfer", "data"), "l1");
        verifySavePocket(pocket("0", "l1", 87L, of("dataKey", "dataValue2")));
        verifySavePocket(pocket("10", "l1", of("dataKey", "dataValue2", "transfer", "data")));

        verifyFindPocketForReload(targetBalance, of("transfer", "data"), "l2");
        verifySavePocket(pocket("0", "l2", 88L));
        verifySavePocket(pocket("10", "l2", of("transfer", "data")));


        verifyFindPocketForReload(targetBalance, of("other", "value", "transfer", "data"), "l3");
        verifySavePocket(pocket("4", "l3", 89L, of("other", "value")));
        verifySavePocket(pocket("6", "l3", of("other", "value", "transfer", "data")));

        verifyNoMoreInteractions(pocketRepository);
        verify(metricService).updateMaxMetric(targetBalance);

        expectBalanceChangeEvents(
            createBalanceEvent("46", 1L, TRANSFER_FROM, of("transfer", "data"), "4", "50",
                               createPocketEvent("10", 85L, null, "l1", "0", "10"),
                               createPocketEvent("10", 86L, null, "l1", of("dataKey", "dataValue"), "0", "10"),
                               createPocketEvent("10", 87L, null, "l1", of("dataKey", "dataValue2"), "0", "10"),
                               createPocketEvent("10", 88L, null, "l2", "0", "10"),
                               createPocketEvent("6", 89L, null, "l3", of("other", "value"), "4", "10")
                              ),
            createBalanceEvent("46", 2L, TRANSFER_TO, of("transfer", "data"), "56", "10",
                               createPocketEvent("10", 185L, null, "l1", of("transfer", "data"), "15", "5"),
                               createPocketEvent("10", 186L, null, "l1", of("dataKey", "dataValue", "transfer", "data"), "15", "5"),
                               createPocketEvent("10", 187L, null, "l1", of("dataKey", "dataValue2", "transfer", "data"), "10", "0"),
                               createPocketEvent("10", 188L, null, "l2", of("transfer", "data"), "10", "0"),
                               createPocketEvent("6", 189L, null, "l3", of("other", "value", "transfer", "data"), "6", "0")
                              )
                                 );
        verifyNoMoreInteractions(balanceChangeEventRepository);
    }

    private void verifySavePocket(Pocket pocket) {
        verify(pocketRepository).save(refEq(pocket, "key", "balance"));
    }

    private void verifyFindPocketForCharging(Balance sourceBalance) {
        verify(pocketRepository).findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100));
    }

    private void verifyFindPocketForReload(Balance sourceBalance, String label,
                                           Map<String, String> metadata, Long pocketId) {
        verify(pocketRepository).findPocketForReload(label, null, null, sourceBalance,
                                                     new Metadata(metadata).getValue());
        verify(pocketRepository).findOneByIdForUpdate(pocketId);
    }


    private void expectSavePocket(Pocket pocketToSave, Pocket savedPocket) {
        when(pocketRepository.save(refEq(pocketToSave, "key", "balance")))
            .thenReturn(savedPocket);
    }

    private void expectSavePocket(String amount, String label, Map<String, String> metadata, long id) {
        when(pocketRepository.save(refEq(pocket(amount, label, id, metadata), "key", "balance")))
            .thenReturn(pocket(amount, label, id, metadata));
    }

    public ReloadBalanceRequest reloadBalanceRequest(long balanceId, String amount, String label) {
        return new ReloadBalanceRequest().setBalanceId(balanceId).setAmount(new BigDecimal(amount)).setLabel(label);
    }

    public ReloadBalanceRequest reloadBalanceRequest(long balanceId, String amount, String label,
                                                     Map<String, String> metadata) {
        return new ReloadBalanceRequest().setBalanceId(balanceId).setAmount(new BigDecimal(amount)).setLabel(label)
            .setMetadata(metadata);
    }

    private void expectPocketForReload(Balance sourceBalance, Pocket pocket, String label, Map<String, String> metadata,
                                       Long pocketId) {
        when(pocketRepository.findPocketForReload(label, null, null, sourceBalance,
                                                  new Metadata(metadata).getValue())).thenReturn(of(id(pocketId)));
        when(pocketRepository.findOneByIdForUpdate(pocketId)).thenReturn(of(pocket));
    }

    private Pocket id(Long id) {
        Pocket pocket = new Pocket();
        pocket.setId(id);
        return pocket;
    }

    private void pocketForReload(Balance sourceBalance, List<Pocket> pockets) {
        when(pocketRepository.findPocketForChargingOrderByDates(sourceBalance, PageRequest.of(0, 100)))
            .thenReturn(new PageImpl<>(pockets));
    }

    private void expectBalance(Balance sourceBalance, String s) {
        when(balanceRepository.findBalanceAmount(sourceBalance)).thenReturn(of(new BigDecimal(s)));
    }

    private Balance createBalance(long sourceId) {
        Balance balance = new Balance();
        balance.setId(sourceId);
        when(balanceRepository.findOneByIdForUpdate(sourceId)).thenReturn(of(balance));
        return balance;
    }

    private Balance createBalanceWithAmount(long sourceId, String balanceAmount) {
        Balance balance = new Balance();
        balance.setId(sourceId);
        balance.setAmount(new BigDecimal(balanceAmount));
        when(balanceRepository.findOneByIdForUpdate(sourceId)).thenReturn(of(balance));
        return balance;
    }

}
