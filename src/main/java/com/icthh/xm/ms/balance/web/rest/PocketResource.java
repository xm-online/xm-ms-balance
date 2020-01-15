package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.ms.balance.service.PocketQueryService;
import com.icthh.xm.ms.balance.service.PocketService;
import com.icthh.xm.ms.balance.service.dto.PocketCriteria;
import com.icthh.xm.ms.balance.service.dto.PocketDTO;
import com.icthh.xm.ms.balance.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Pocket.
 */
@RestController
@RequestMapping("/api")
public class PocketResource {

    private static final String ENTITY_NAME = "pocket";

    private final PocketService pocketService;

    private final PocketQueryService pocketQueryService;

    public PocketResource(PocketService pocketService, PocketQueryService pocketQueryService) {
        this.pocketService = pocketService;
        this.pocketQueryService = pocketQueryService;
    }

    /**
     * POST  /pockets : Create a new pocket.
     *
     * @param pocketDTO the pocketDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new pocketDTO, or with status 400 (Bad Request) if the pocket has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'pocket': #pocket}, 'POCKET.CREATE')")
    @PostMapping("/pockets")
    @Timed
    @PrivilegeDescription("Privilege to create a new pocket")
    public ResponseEntity<PocketDTO> createPocket(@Valid @RequestBody PocketDTO pocketDTO) throws URISyntaxException {
        if (pocketDTO.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                "A new pocket cannot already have an ID");
        }
        PocketDTO result = pocketService.save(pocketDTO);
        return ResponseEntity.created(new URI("/api/pockets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pockets : Updates an existing pocket.
     *
     * @param pocketDTO the pocketDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated pocketDTO,
     * or with status 400 (Bad Request) if the pocketDTO is not valid,
     * or with status 500 (Internal Server Error) if the pocketDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'id': #pocket.id, 'newPocket': #pocket}, 'pocket', 'POCKET.UPDATE')")
    @PutMapping("/pockets")
    @Timed
    @PrivilegeDescription("Privilege to updates an existing pocket")
    public ResponseEntity<PocketDTO> updatePocket(@Valid @RequestBody PocketDTO pocketDTO) throws URISyntaxException {
        if (pocketDTO.getId() == null) {
            return createPocket(pocketDTO);
        }
        PocketDTO result = pocketService.save(pocketDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, pocketDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pockets : get all the pockets.
     *
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of pockets in body
     */
    @GetMapping("/pockets")
    @Timed
    public ResponseEntity<List<PocketDTO>> getAllPockets(PocketCriteria criteria) {
        List<PocketDTO> entityList = pocketQueryService.findByCriteria(criteria, null);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * GET  /pockets/:id : get the "id" pocket.
     *
     * @param id the id of the pocketDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the pocketDTO, or with status 404 (Not Found)
     */
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'POCKET.GET_LIST.ITEM')")
    @GetMapping("/pockets/{id}")
    @Timed
    @PrivilegeDescription("Privilege to get the pocket by id")
    public ResponseEntity<PocketDTO> getPocket(@PathVariable Long id) {
        PocketDTO pocketDTO = pocketService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(pocketDTO));
    }

    /**
     * DELETE  /pockets/:id : delete the "id" pocket.
     *
     * @param id the id of the pocketDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasPermission({'id': #id}, 'pocket', 'POCKET.DELETE')")
    @DeleteMapping("/pockets/{id}")
    @Timed
    @PrivilegeDescription("Privilege to delete the pocket by id")
    public ResponseEntity<Void> deletePocket(@PathVariable Long id) {
        pocketService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
