package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.ms.balance.service.MetricQueryService;
import com.icthh.xm.ms.balance.service.MetricService;
import com.icthh.xm.ms.balance.service.dto.MetricCriteria;
import com.icthh.xm.ms.balance.service.dto.MetricDTO;
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
 * REST controller for managing Metric.
 */
@RestController
@RequestMapping("/api")
public class MetricResource {

    private static final String ENTITY_NAME = "metric";

    private final MetricService metricService;

    private final MetricQueryService metricQueryService;

    public MetricResource(MetricService metricService, MetricQueryService metricQueryService) {
        this.metricService = metricService;
        this.metricQueryService = metricQueryService;
    }

    /**
     * POST  /metrics : Create a new metric.
     *
     * @param metricDTO the metricDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new metricDTO, or with status 400 (Bad Request) if the metric has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'metric': #metric}, 'METRIC.CREATE')")
    @PostMapping("/metrics")
    @Timed
    @PrivilegeDescription("Privilege to create a new metric")
    public ResponseEntity<MetricDTO> createMetric(@Valid @RequestBody MetricDTO metricDTO) throws URISyntaxException {
        if (metricDTO.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                "A new metric cannot already have an ID");
        }
        MetricDTO result = metricService.save(metricDTO);
        return ResponseEntity.created(new URI("/api/metrics/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /metrics : Updates an existing metric.
     *
     * @param metricDTO the metricDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated metricDTO,
     * or with status 400 (Bad Request) if the metricDTO is not valid,
     * or with status 500 (Internal Server Error) if the metricDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'id': #metric.id, 'newMetric': #metric}, 'metric', 'METRIC.UPDATE')")
    @PutMapping("/metrics")
    @Timed
    @PrivilegeDescription("Privilege to updates an existing metric")
    public ResponseEntity<MetricDTO> updateMetric(@Valid @RequestBody MetricDTO metricDTO) throws URISyntaxException {
        if (metricDTO.getId() == null) {
            return createMetric(metricDTO);
        }
        MetricDTO result = metricService.save(metricDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, metricDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /metrics : get all the metrics.
     *
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of metrics in body
     */
    @GetMapping("/metrics")
    @Timed
    public ResponseEntity<List<MetricDTO>> getAllMetrics(MetricCriteria criteria) {
        List<MetricDTO> entityList = metricQueryService.findByCriteria(criteria, null);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * GET  /metrics/:id : get the "id" metric.
     *
     * @param id the id of the metricDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the metricDTO, or with status 404 (Not Found)
     */
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'METRIC.GET_LIST.ITEM')")
    @GetMapping("/metrics/{id}")
    @Timed
    @PrivilegeDescription("Privilege to get the metric by id")
    public ResponseEntity<MetricDTO> getMetric(@PathVariable Long id) {
        MetricDTO metricDTO = metricService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(metricDTO));
    }

    /**
     * DELETE  /metrics/:id : delete the "id" metric.
     *
     * @param id the id of the metricDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasPermission({'id': #id}, 'metric', 'METRIC.DELETE')")
    @DeleteMapping("/metrics/{id}")
    @Timed
    @PrivilegeDescription("Privilege to delete the metric by id")
    public ResponseEntity<Void> deleteMetric(@PathVariable Long id) {
        metricService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
