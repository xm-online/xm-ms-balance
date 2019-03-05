package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.repository.BalanceRepository;
import com.icthh.xm.ms.balance.repository.MetricRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("5")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(empty());

        metricService.updateMaxMetric(balance);

        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("5"), "key", "balance"));
    }

    @Test
    public void ifNewBalanceMoreThenInMetricMetricsUpdated() {
        Balance balance = new Balance();
        balance.setId(456L);
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("18")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().typeKey("MAX").value("17")));

        metricService.updateMaxMetric(balance);

        verify(metricRepository).save(refEq(new Metric().typeKey("MAX").value("18"), "key", "balance"));
    }

    @Test
    public void ifNewBalanceLessThenInMetricMetricsNotChanged() {
        Balance balance = new Balance();
        balance.setId(456L);
        when(balanceRepository.findBalanceAmount(balance)).thenReturn(of(new BigDecimal("17")));
        when(metricRepository.findByTypeKeyAndBalance("MAX", balance)).thenReturn(of(new Metric().typeKey("MAX").value("18")));

        metricService.updateMaxMetric(balance);

        verify(metricRepository).findByTypeKeyAndBalance(eq("MAX"), refEq(balance));
        verifyNoMoreInteractions(metricRepository);

    }

}
