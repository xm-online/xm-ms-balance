package com.icthh.xm.ms.balance.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.commons.i18n.error.web.ExceptionTranslator;
import com.icthh.xm.commons.i18n.spring.service.LocalizationMessageService;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.web.rest.requests.HistoryRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static com.icthh.xm.ms.balance.service.OperationType.RELOAD;
import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@EnableSpringDataWebSupport
@WebMvcTest(controllers = BalanceHistoryResource.class)
@ContextConfiguration(classes = {BalanceHistoryResource.class, ExceptionTranslator.class})
@SuppressWarnings("unused")
public class BalanceHistoryResourceMvcTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private LocalizationMessageService localizationMessageService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    private MockMvc mockMvc;

    @Before
    public void setup() {

        // Setup MockMVC to use our Spring Configuration
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }


    @Test
    @SneakyThrows
    public void testPocketHistory() {
        when(balanceHistoryService.getPocketChangesByTypeAndDate(createTestRequest(), PageRequest.of(1, 50)))
            .thenReturn(new PageImpl<>(singletonList(
                createPocket()
            )));
        String url = "/api/pockets/history";

        ResultActions resultActions = mockMvc.perform(testRequestUrl(url)).andDo(print()).andExpect(status().isOk());

        assertPocketEvent(resultActions);
        verify(balanceHistoryService).getPocketChangesByTypeAndDate(refEq(createTestRequest()), refEq(PageRequest.of(1, 50)));
    }

    @Test
    @SneakyThrows
    public void testSearchPocketHistoryByTemplateName() {

        when(balanceHistoryService.findPocketChanges(eq("templateName"), any(), refEq(PageRequest.of(1, 50))))
            .thenReturn(new PageImpl<>(singletonList(
                createPocket()
            )));

        String url = "/api/pockets/history/templateName";
        ResultActions resultActions = mockMvc.perform(testTemplateRequestUrl(url)).andDo(print()).andExpect(status().isOk());
        assertPocketEvent(resultActions);


        verify(balanceHistoryService).findPocketChanges(eq("templateName"), mapArgumentCaptor.capture(), refEq(PageRequest.of(1, 50)));
        Map<String, Object> actual = mapArgumentCaptor.getValue();
        assertEquals(actual.size(), 4);
        assertEquals(actual.get("operationType"), "RELOAD");
        assertEquals(actual.get("startDate"), ofEpochSecond(100500).toString());
        assertEquals(actual.get("endDate"), ofEpochSecond(100502).toString());
        assertArrayEquals((Object[]) actual.get("entityIds"), asList("1", "2").toArray());
    }


    @Test
    @SneakyThrows
    public void testBalancesHistory() {
        when(balanceHistoryService.getBalanceChangesByTypeAndDate(createTestRequest(), PageRequest.of(1, 50)))
            .thenReturn(new PageImpl<>(singletonList(
                createTestBalanceEvent()
            )));
        String url = "/api/balances/history";

        ResultActions resultActions = mockMvc.perform(testRequestUrl(url)).andDo(print()).andExpect(status().isOk());

        balanceAsserts("$.[*]", resultActions);
        verify(balanceHistoryService).getBalanceChangesByTypeAndDate(refEq(createTestRequest()), refEq(PageRequest.of(1, 50)));
    }

    @Test
    @SneakyThrows
    public void testSearchBalancesHistoryByTemplateName() {

        when(balanceHistoryService.findBalanceChanges(eq("templateName"), any(), refEq(PageRequest.of(1, 50))))
            .thenReturn(new PageImpl<>(singletonList(
                createTestBalanceEvent()
            )));

        String url = "/api/balances/history/templateName";
        ResultActions resultActions = mockMvc.perform(testTemplateRequestUrl(url)).andDo(print()).andExpect(status().isOk());
        balanceAsserts("$.[*]", resultActions);


        verify(balanceHistoryService).findBalanceChanges(eq("templateName"), mapArgumentCaptor.capture(), refEq(PageRequest.of(1, 50)));
        Map<String, Object> actual = mapArgumentCaptor.getValue();
        assertEquals(actual.size(), 4);
        assertEquals(actual.get("operationType"), "RELOAD");
        assertEquals(actual.get("startDate"), ofEpochSecond(100500).toString());
        assertEquals(actual.get("endDate"), ofEpochSecond(100502).toString());
        assertArrayEquals((Object[]) actual.get("entityIds"), asList("1", "2").toArray());
    }

    public PocketChangeEvent createPocket() {
        return PocketChangeEvent.builder()
            .amountDelta(new BigDecimal("5.21"))
            .id(1L)
            .pocketId(3L)
            .pocketKey("keyP")
            .pocketLabel("labelP")
            .transaction(createTestBalanceEvent())
            .build();
    }

    public BalanceChangeEvent createTestBalanceEvent() {
        return BalanceChangeEvent.builder()
            .operationType(RELOAD)
            .amountDelta(new BigDecimal("5.21"))
            .balanceEntityId(4L)
            .balanceId(5L)
            .balanceKey("KEY")
            .balanceTypeKey("TYPEKEY")
            .executedByUserKey("userKey")
            .operationDate(Instant.ofEpochSecond(1005001234L))
            .operationId("84a6412c-d467-41b4-bd93-9658a6f2e23f")
            .build();
    }

    public ResultActions assertPocketEvent(ResultActions resultActions) throws Exception {
        ResultActions asserts = resultActions
            .andExpect(jsonPath("$.[*].amountDelta").value(hasItem(new BigDecimal("5.21").doubleValue())))
            .andExpect(jsonPath("$.[*].id").value(hasItem(1)))
            .andExpect(jsonPath("$.[*].pocketId").value(hasItem(3)))
            .andExpect(jsonPath("$.[*].pocketLabel").value(hasItem("labelP")))
            .andExpect(jsonPath("$.[*].pocketKey").value(hasItem("keyP")));
        return balanceAsserts("$.[*].transaction", asserts);
    }

    public ResultActions balanceAsserts(String transactionPrefix, ResultActions asserts) throws Exception {
        return asserts
            .andExpect(jsonPath(transactionPrefix + ".operationType").value(hasItem("RELOAD")))
            .andExpect(jsonPath(transactionPrefix + ".amountDelta").value(hasItem(new BigDecimal("5.21").doubleValue())))
            .andExpect(jsonPath(transactionPrefix + ".balanceEntityId").value(hasItem(4)))
            .andExpect(jsonPath(transactionPrefix + ".balanceId").value(hasItem(5)))
            .andExpect(jsonPath(transactionPrefix + ".balanceKey").value(hasItem("KEY")))
            .andExpect(jsonPath(transactionPrefix + ".balanceTypeKey").value(hasItem("TYPEKEY")))
            .andExpect(jsonPath(transactionPrefix + ".executedByUserKey").value(hasItem("userKey")))
            .andExpect(jsonPath(transactionPrefix + ".operationDate").value(hasItem("2001-11-05T23:00:34Z")))
            .andExpect(jsonPath(transactionPrefix + ".operationId").value(hasItem("84a6412c-d467-41b4-bd93-9658a6f2e23f")));
    }

    public HistoryRequest createTestRequest() {
        return HistoryRequest.builder()
            .endDate(ofEpochSecond(100502))
            .startDate(ofEpochSecond(100500))
            .operationType(RELOAD)
            .entityIds(asList(1L, 2L))
            .build();
    }

    public MockHttpServletRequestBuilder testRequestUrl(String url) {
        return get(url + "?operationType=RELOAD&startDate={startDate}&endDate={endDate}&entityIds=1&entityIds=2&size=50&page=1",
            ofEpochSecond(100500), ofEpochSecond(100502));
    }

    public MockHttpServletRequestBuilder testTemplateRequestUrl(String url) {
        return get(url + "?templateParams[operationType]=RELOAD&templateParams[startDate]={startDate}" +
                "&templateParams[endDate]={endDate}&templateParams[entityIds]=1&templateParams[entityIds]=2" +
                "&size=50&page=1",
            ofEpochSecond(100500), ofEpochSecond(100502));
    }

}
