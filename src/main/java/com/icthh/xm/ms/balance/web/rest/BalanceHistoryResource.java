package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.web.rest.requests.HistoryRequest;
import com.icthh.xm.ms.balance.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Balance.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BalanceHistoryResource {

    private final BalanceHistoryService balanceHistoryService;

    @GetMapping("/balances/history")
    @Timed
    public ResponseEntity<List<BalanceChangeEvent>> searchBalanceHistory(@ApiParam HistoryRequest request,
                                                                   Pageable pageable) {
        Page<BalanceChangeEvent> page = balanceHistoryService.getBalanceChangesByTypeAndDate(request, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances/history");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/balances/history/{templateName}")
    @Timed
    public ResponseEntity<List<BalanceChangeEvent>> searchBalanceHistory(@PathVariable("templateName") String templateName,
                                                                   @ApiParam Map<String, Object> params,
                                                                   Pageable pageable) {
        Page<BalanceChangeEvent> page = balanceHistoryService.findBalanceChanges(templateName, params, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances/history/" + templateName);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/pockets/history")
    @Timed
    public ResponseEntity<List<PocketChangeEvent>> searchPocketHistory(@ApiParam HistoryRequest request,
                                                                       Pageable pageable) {
        Page<PocketChangeEvent> page = balanceHistoryService.getPocketChangesByTypeAndDate(request, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pockets/history");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/pockets/history/{templateName}")
    @Timed
    public ResponseEntity<List<PocketChangeEvent>> searchPocketHistory(@PathVariable("templateName") String templateName,
                                                                        @ApiParam Map<String, Object> params,
                                                                        Pageable pageable) {
        Page<PocketChangeEvent> page = balanceHistoryService.findPocketChanges(templateName, params, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pockets/history/" + templateName);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
