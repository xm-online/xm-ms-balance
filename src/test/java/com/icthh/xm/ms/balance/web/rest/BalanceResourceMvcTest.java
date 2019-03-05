package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.i18n.spring.service.LocalizationMessageService;
import com.icthh.xm.ms.balance.service.BalanceQueryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.NoEnoughMoneyException;
import com.icthh.xm.ms.balance.service.OperationType;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import static java.time.Instant.now;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the BalanceResource REST controller.
 *
 * @see BalanceResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = BalanceResource.class)
@ContextConfiguration(classes = {BalanceResource.class, ExceptionTranslator.class})
public class BalanceResourceMvcTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        // Setup MockMVC to use our Spring Configuration
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private BalanceQueryService balanceQueryService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private LocalizationMessageService localizationMessageService;

    @Test
    @SneakyThrows
    public void testReloadBalance() {
        Instant endDateTime = now();
        Instant startDateTime = now();
        ReloadBalanceRequest reloadBalanceRequest = createReloadBalanceRequest(endDateTime, startDateTime);

        doReturn(createBalanceChangeEventDto()).when(balanceService).reload(reloadBalanceRequest);

        mockMvc.perform(post("/api/balances/reload")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reloadBalanceRequest)))
            .andExpect(status().isOk());

        verify(balanceService).reload(refEq(createReloadBalanceRequest(endDateTime, startDateTime)));
    }

    private ReloadBalanceRequest createReloadBalanceRequest(Instant endDateTime, Instant startDateTime) {
        return new ReloadBalanceRequest()
            .setBalanceId(5L)
            .setAmount(new BigDecimal("50"))
            .setEndDateTime(endDateTime)
            .setStartDateTime(startDateTime)
            .setLabel("label");
    }

    private BalanceChangeEventDto createBalanceChangeEventDto() {
        return new BalanceChangeEventDto()
            .setOperationId(UUID.randomUUID().toString())
            .setOperationType(OperationType.RELOAD)
            .setAmountDelta(new BigDecimal("50"))
            .setBalanceId(5L)
            .setOperationDate(Instant.now());
    }

    @Test
    @SneakyThrows
    public void validationFailedIfNoRequiredArgument() {
        expectValidationError((reloadBalanceRequest) -> reloadBalanceRequest.setLabel(null));
        expectValidationError((reloadBalanceRequest) -> reloadBalanceRequest.setAmount(null));
        expectValidationError((reloadBalanceRequest) -> reloadBalanceRequest.setBalanceId(null));
    }

    private void expectValidationError(Consumer<ReloadBalanceRequest> task) throws Exception {
        Instant endDateTime = now();
        Instant startDateTime = now();
        ReloadBalanceRequest reloadBalanceRequest = createReloadBalanceRequest(endDateTime, startDateTime);
        task.accept(reloadBalanceRequest);
        mockMvc.perform(post("/api/balances/reload")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reloadBalanceRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void notFoundResultCodeIfBalanceNotFound() {
        ReloadBalanceRequest reloadBalanceRequest = createReloadBalanceRequest(now(), now());

        doThrow(new EntityNotFoundException("")).when(balanceService).reload(reloadBalanceRequest);

        mockMvc.perform(post("/api/balances/reload")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(reloadBalanceRequest)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void bagRequestIfNoMany() {
        ChargingBalanceRequest request = new ChargingBalanceRequest(1L, new BigDecimal("5"));

        doThrow(new NoEnoughMoneyException(1L, new BigDecimal("3.21"))).when(balanceService).charging(request);

        mockMvc.perform(post("/api/balances/charging")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("error.no.enough.money"))
            .andExpect(jsonPath("$.params.currentAmount").value("3.21"))
            .andExpect(jsonPath("$.params.balanceId").value("1"));

        //{"error":"error.no.enough.many","error_description":"No enough many","params":{"currentAmount":"3.21","balanceId":"1"}
    }

}
