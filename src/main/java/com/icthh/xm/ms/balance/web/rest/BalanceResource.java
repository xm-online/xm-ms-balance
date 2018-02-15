package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.ms.balance.domain.Balance;
import com.icthh.xm.ms.balance.service.BalanceService;
import com.icthh.xm.ms.balance.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Balance.
 */
@RestController
@RequestMapping("/api")
public class BalanceResource {

    private static final String ENTITY_NAME = "balance";

    private final BalanceService balanceService;

    public BalanceResource(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * POST  /balances : Create a new balance.
     *
     * @param balance the balance to create
     * @return the ResponseEntity with status 201 (Created) and with body the new balance, or with status 400 (Bad Request) if the balance has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/balances")
    @Timed
    @PreAuthorize("hasPermission({'balance': #balance}, 'BALANCE.CREATE')")
    public ResponseEntity<Balance> createBalance(@Valid @RequestBody Balance balance) throws URISyntaxException {
        if (balance.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                                        "A new balance cannot already have an ID");
        }
        Balance result = balanceService.save(balance);
        return ResponseEntity.created(new URI("/api/balances/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /balances : Updates an existing balance.
     *
     * @param balance the balance to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated balance,
     * or with status 400 (Bad Request) if the balance is not valid,
     * or with status 500 (Internal Server Error) if the balance couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/balances")
    @Timed
    @PreAuthorize("hasPermission({'id': #balance.id, 'newBalance': #balance}, 'balance', 'BALANCE.UPDATE')")
    public ResponseEntity<Balance> updateBalance(@Valid @RequestBody Balance balance) throws URISyntaxException {
        if (balance.getId() == null) {
            return createBalance(balance);
        }
        Balance result = balanceService.save(balance);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, balance.getId().toString()))
            .body(result);
    }

    /**
     * GET  /balances : get all the balances.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of balances in body
     */
    @GetMapping("/balances")
    @Timed
    public List<Balance> getAllBalances() {
        return balanceService.findAll(null);
    }

    /**
     * GET  /balances/:id : get the "id" balance.
     *
     * @param id the id of the balance to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the balance, or with status 404 (Not Found)
     */
    @GetMapping("/balances/{id}")
    @Timed
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'BALANCE.GET_LIST.ITEM')")
    public ResponseEntity<Balance> getBalance(@PathVariable Long id) {
        Balance balance = balanceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(balance));
    }

    /**
     * DELETE  /balances/:id : delete the "id" balance.
     *
     * @param id the id of the balance to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/balances/{id}")
    @Timed
    @PreAuthorize("hasPermission({'id': #id}, 'balance', 'BALANCE.DELETE')")
    public ResponseEntity<Void> deleteBalance(@PathVariable Long id) {
        balanceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
