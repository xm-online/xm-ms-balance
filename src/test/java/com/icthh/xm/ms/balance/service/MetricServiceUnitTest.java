package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class MetricServiceUnitTest {

    @InjectMocks
    private MetricService metricService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private MetricRepository metricRepository;

    @Test
    public void ifMetricsNotExistsCreateNew() {
        Balance balance = new Balance();
        balance.setId(456L);
        Instant now = Instant.now();
        when(balanceRepository.findBalanceAmount(balance, now)).thenReturn(of(new BigDecimal("5")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(empty());

        metricService.updateMaxMetric(balance, now);

        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("5"), "key", "balance"));
    }

    @Test
    public void ifNewBalanceMoreThenInMetricMetricsUpdated() {
        Balance balance = new Balance();
        balance.setId(456L);
        Instant now = Instant.now();
        when(balanceRepository.findBalanceAmount(balance, now)).thenReturn(of(new BigDecimal("18")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().typeKey("MAX").value("17")));

        metricService.updateMaxMetric(balance, now);

        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("18"), "key", "balance"));
    }

    @Test
    public void ifNewBalanceLessThenInMetricMetricsNotChanged() {
        Balance balance = new Balance();
        balance.setId(456L);
        Instant now = Instant.now();
        when(balanceRepository.findBalanceAmount(balance, now)).thenReturn(of(new BigDecimal("17")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().typeKey("MAX").value("18")));

        metricService.updateMaxMetric(balance, now);

        verify(metricRepository).findByTypeKeyAndBalance(eq("MAX"), refEq(balance));
        verifyNoMoreInteractions(metricRepository);

    }

}
