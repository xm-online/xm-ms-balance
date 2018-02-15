package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.ms.balance.domain.Pocket;
import com.icthh.xm.ms.balance.service.PocketService;
import com.icthh.xm.ms.balance.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Pocket.
 */
@RestController
@RequestMapping("/api")
public class PocketResource {

    private static final String ENTITY_NAME = "pocket";

    private final PocketService pocketService;

    public PocketResource(PocketService pocketService) {
        this.pocketService = pocketService;
    }

    /**
     * POST  /pockets : Create a new pocket.
     *
     * @param pocket the pocket to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pocket, or with status 400 (Bad Request) if the pocket has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pockets")
    @Timed
    @PreAuthorize("hasPermission({'pocket': #pocket}, 'POCKET.CREATE')")
    public ResponseEntity<Pocket> createPocket(@Valid @RequestBody Pocket pocket) throws URISyntaxException {
        if (pocket.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                                        "A new balance cannot already have an ID");
        }
        Pocket result = pocketService.save(pocket);
        return ResponseEntity.created(new URI("/api/pockets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pockets : Updates an existing pocket.
     *
     * @param pocket the pocket to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pocket,
     * or with status 400 (Bad Request) if the pocket is not valid,
     * or with status 500 (Internal Server Error) if the pocket couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pockets")
    @Timed
    @PreAuthorize("hasPermission({'id': #pocket.id, 'newPocket': #pocket}, 'pocket', 'POCKET.UPDATE')")
    public ResponseEntity<Pocket> updatePocket(@Valid @RequestBody Pocket pocket) throws URISyntaxException {
        if (pocket.getId() == null) {
            return createPocket(pocket);
        }
        Pocket result = pocketService.save(pocket);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, pocket.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pockets : get all the pockets.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of pockets in body
     */
    @GetMapping("/pockets")
    @Timed
    public List<Pocket> getAllPockets() {
        return pocketService.findAll(null);
    }

    /**
     * GET  /pockets/:id : get the "id" pocket.
     *
     * @param id the id of the pocket to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pocket, or with status 404 (Not Found)
     */
    @GetMapping("/pockets/{id}")
    @Timed
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'POCKET.GET_LIST.ITEM')")
    public ResponseEntity<Pocket> getPocket(@PathVariable Long id) {
        Pocket pocket = pocketService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pocket));
    }

    /**
     * DELETE  /pockets/:id : delete the "id" pocket.
     *
     * @param id the id of the pocket to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pockets/{id}")
    @Timed
    @PreAuthorize("hasPermission({'id': #id}, 'pocket', 'POCKET.DELETE')")
    public ResponseEntity<Void> deletePocket(@PathVariable Long id) {
        pocketService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
