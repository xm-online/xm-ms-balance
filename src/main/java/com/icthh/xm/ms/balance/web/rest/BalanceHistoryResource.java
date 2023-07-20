package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent;
import com.icthh.xm.ms.balance.domain.PocketChangeEvent;
import com.icthh.xm.ms.balance.service.BalanceHistoryService;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;
import com.icthh.xm.ms.balance.service.dto.BalanceHistoryCriteria;
import com.icthh.xm.ms.balance.web.rest.requests.HistoryRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TemplateParamsHolder;
import com.icthh.xm.ms.balance.web.rest.util.PaginationUtil;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Balance.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BalanceHistoryResource {

    private final BalanceHistoryService balanceHistoryService;

    @PreAuthorize("hasPermission({'request': #request}, 'BALANCE_HISTORY.SEARCH_BY_DATE')")
    @GetMapping("/balances/history")
    @Timed
    @PrivilegeDescription("Privilege to search balance history by date")
    public ResponseEntity<List<BalanceChangeEvent>> searchBalanceHistory(HistoryRequest request,
                                                                         Pageable pageable) {
        Page<BalanceChangeEvent> page = balanceHistoryService.getBalanceChangesByTypeAndDate(request, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances/history");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    @PreAuthorize("hasPermission({'criteria': #criteria}, 'BALANCE_HISTORY.CRITERIA')")
    @GetMapping("/v2/balances/history")
    @Timed
    @PrivilegeDescription("Privilege to search balance history by criteria")
    public ResponseEntity<List<BalanceChangeEventDto>> searchBalanceHistoryByCriteria(BalanceHistoryCriteria criteria,
                                                                                      Pageable pageable) {
        Page<BalanceChangeEventDto> page = balanceHistoryService.getBalanceChangesByCriteria(criteria, pageable, null);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances/history");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission({'templateName': #templateName, 'params': #params}, 'BALANCE_HISTORY.SEARCH_BY_TEMPLATE')")
    @GetMapping("/balances/history/{templateName}")
    @Timed
    @PrivilegeDescription("Privilege to search balance history by templateName")
    public ResponseEntity<List<BalanceChangeEvent>> searchBalanceHistory(@PathVariable("templateName") String templateName,
                                                                         TemplateParamsHolder params,
                                                                         Pageable pageable) {
        Page<BalanceChangeEvent> page = balanceHistoryService.findBalanceChanges(templateName, params.getTemplateParams(), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances/history/" + templateName);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission({'request': #request}, 'POCKET_HISTORY.SEARCH_BY_DATE')")
    @GetMapping("/pockets/history")
    @Timed
    @PrivilegeDescription("Privilege to search pocket history by date")
    public ResponseEntity<List<PocketChangeEvent>> searchPocketHistory(HistoryRequest request,
                                                                       Pageable pageable) {
        Page<PocketChangeEvent> page = balanceHistoryService.getPocketChangesByTypeAndDate(request, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pockets/history");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission({'templateName': #templateName, 'params': #params}, 'POCKET_HISTORY.SEARCH_BY_TEMPLATE')")
    @GetMapping("/pockets/history/{templateName}")
    @Timed
    @PrivilegeDescription("Privilege to search pocket history by templateName")
    public ResponseEntity<List<PocketChangeEvent>> searchPocketHistory(@PathVariable("templateName") String templateName,
                                                                       TemplateParamsHolder params,
                                                                       Pageable pageable) {
        Page<PocketChangeEvent> page = balanceHistoryService.findPocketChanges(templateName, params.getTemplateParams(), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pockets/history/" + templateName);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
