package com.icthh.xm.ms.balance.web.rest;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.icthh.xm.commons.exceptions.EntityNotFoundException;
import com.icthh.xm.commons.exceptions.spring.web.ExceptionTranslator;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.BalanceQueryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.NoEnoughMoneyException;
import com.icthh.xm.ms.balance.service.OperationType;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.HistoryRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Test class for the BalanceResource REST controller.
 *
 * @see BalanceResource
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = BalanceHistoryResource.class)
@ContextConfiguration(classes = {BalanceHistoryResource.class, ExceptionTranslator.class})
public class BalanceHistoryResourceMvcTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        // Setup MockMVC to use our Spring Configuration
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    @MockBean
    private MessageSource messageSource;

    @Test
    @SneakyThrows
    public void testReloadBalance() {

        mockMvc.perform(get("/api/pockets/history?operationType=RELOAD&startDate={startDate}&endDate={endDate}&entityIds=1&entityIds=2&size=50&page=1",
            now(), now())).andExpect(status().isOk());



    }


}
