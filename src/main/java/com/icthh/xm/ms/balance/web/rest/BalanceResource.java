package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.ms.balance.service.BalanceQueryService;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.service.dto.BalanceChangeEventDto;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;
import com.icthh.xm.ms.balance.service.dto.BalanceDTO;
import com.icthh.xm.ms.balance.service.dto.TransferDto;
import com.icthh.xm.ms.balance.web.rest.requests.ChargingBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.ReloadBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.requests.TransferBalanceRequest;
import com.icthh.xm.ms.balance.web.rest.util.HeaderUtil;
import com.icthh.xm.ms.balance.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Balance.
 */
@RestController
@RequestMapping("/api")
public class BalanceResource {

    private static final String ENTITY_NAME = "balance";

    private final BalanceService balanceService;

    private final BalanceQueryService balanceQueryService;

    public BalanceResource(BalanceService balanceService, BalanceQueryService balanceQueryService) {
        this.balanceService = balanceService;
        this.balanceQueryService = balanceQueryService;
    }

    /**
     * POST  /balances : Create a new balance.
     *
     * @param balanceDTO the balanceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new balanceDTO, or with status 400 (Bad Request) if the balance has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'balance': #balance}, 'BALANCE.CREATE')")
    @PostMapping("/balances")
    @Timed
    @PrivilegeDescription("Privilege to create a new balance")
    public ResponseEntity<BalanceDTO> createBalance(@Valid @RequestBody BalanceDTO balanceDTO) throws URISyntaxException {
        if (balanceDTO.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                "A new balance cannot already have an ID");
        }
        BalanceDTO result = balanceService.save(balanceDTO);
        return ResponseEntity.created(new URI("/api/balances/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /balances : Updates an existing balance.
     *
     * @param balanceDTO the balanceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated balanceDTO,
     * or with status 400 (Bad Request) if the balanceDTO is not valid,
     * or with status 500 (Internal Server Error) if the balanceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'id': #balance.id, 'newBalance': #balance}, 'balance', 'BALANCE.UPDATE')")
    @PutMapping("/balances")
    @Timed
    @PrivilegeDescription("Privilege to updates an existing balance")
    public ResponseEntity<BalanceDTO> updateBalance(@Valid @RequestBody BalanceDTO balanceDTO) throws URISyntaxException {
        if (balanceDTO.getId() == null) {
            return createBalance(balanceDTO);
        }
        BalanceDTO result = balanceService.save(balanceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, balanceDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /balances : get all the balances.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of balances in body
     */
    @GetMapping("/balances")
    @Timed
    public ResponseEntity<List<BalanceDTO>> getAllBalances(BalanceCriteria criteria, Pageable pageable) {
        Page<BalanceDTO> page = balanceQueryService.findByCriteria(criteria, pageable, null);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/balances");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /balances/:id : get the "id" balance.
     *
     * @param id the id of the balanceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the balanceDTO, or with status 404 (Not Found)
     */
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'BALANCE.GET_LIST.ITEM')")
    @GetMapping("/balances/{id}")
    @Timed
    @PrivilegeDescription("Privilege to get the balance by id")
    public ResponseEntity<BalanceDTO> getBalance(@PathVariable Long id, @RequestParam(value = "applyDate", required = false) Instant applyDate) {
        BalanceDTO balanceDTO = balanceService.findOne(id, applyDate);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(balanceDTO));
    }

    /**
     * DELETE  /balances/:id : delete the "id" balance.
     *
     * @param id the id of the balanceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasPermission({'id': #id}, 'balance', 'BALANCE.DELETE')")
    @DeleteMapping("/balances/{id}")
    @Timed
    @PrivilegeDescription("Privilege to delete the balance by id")
    public ResponseEntity<Void> deleteBalance(@PathVariable Long id) {
        balanceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PreAuthorize("hasPermission({'reloadRequest': #reloadRequest}, 'BALANCE.RELOAD')")
    @PostMapping("/balances/reload")
    @Timed
    @PrivilegeDescription("Privilege to reload the balance")
    public ResponseEntity<BalanceChangeEventDto> reloadBalance(@Valid @RequestBody ReloadBalanceRequest reloadRequest) {
        BalanceChangeEventDto balanceChangeEventDto = balanceService.reload(reloadRequest);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(balanceChangeEventDto));
    }

    @PreAuthorize("hasPermission({'chargingRequest': #chargingRequest}, 'BALANCE.CHARGING')")
    @PostMapping("/balances/charging")
    @Timed
    @PrivilegeDescription("Privilege to charging the balance")
    public ResponseEntity<BalanceChangeEventDto> chargingBalance(@Valid @RequestBody ChargingBalanceRequest chargingRequest) {
        BalanceChangeEventDto balanceChangeEventDto = balanceService.charging(chargingRequest);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(balanceChangeEventDto));
    }

    @PreAuthorize("hasPermission({'transferRequest': #transferRequest}, 'BALANCE.TRANSFER')")
    @PostMapping("/balances/transfer")
    @Timed
    @PrivilegeDescription("Privilege to transfer the balance")
    public ResponseEntity<TransferDto> transferBalance(@Valid @RequestBody TransferBalanceRequest transferRequest) {
        TransferDto response = balanceService.transfer(transferRequest);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(response));
    }

}
