package com.icthh.xm.ms.balance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import com.icthh.xm.ms.balance.domain.Metric;
import com.icthh.xm.ms.balance.service.MetricService;
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
 * REST controller for managing Metric.
 */
@RestController
@RequestMapping("/api")
public class MetricResource {

    private static final String ENTITY_NAME = "metric";

    private final MetricService metricService;

    public MetricResource(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * POST  /metrics : Create a new metric.
     *
     * @param metric the metric to create
     * @return the ResponseEntity with status 201 (Created) and with body the new metric, or with status 400 (Bad Request) if the metric has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/metrics")
    @Timed
    @PreAuthorize("hasPermission({'metric': #metric}, 'METRIC.CREATE')")
    public ResponseEntity<Metric> createMetric(@Valid @RequestBody Metric metric) throws URISyntaxException {
        if (metric.getId() != null) {
            throw new BusinessException(ErrorConstants.ERR_BUSINESS_IDEXISTS,
                                        "A new balance cannot already have an ID");
        }
        Metric result = metricService.save(metric);
        return ResponseEntity.created(new URI("/api/metrics/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /metrics : Updates an existing metric.
     *
     * @param metric the metric to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated metric,
     * or with status 400 (Bad Request) if the metric is not valid,
     * or with status 500 (Internal Server Error) if the metric couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/metrics")
    @Timed
    @PreAuthorize("hasPermission({'id': #metric.id, 'newMetric': #metric}, 'metric', 'METRIC.UPDATE')")
    public ResponseEntity<Metric> updateMetric(@Valid @RequestBody Metric metric) throws URISyntaxException {
        if (metric.getId() == null) {
            return createMetric(metric);
        }
        Metric result = metricService.save(metric);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, metric.getId().toString()))
            .body(result);
    }

    /**
     * GET  /metrics : get all the metrics.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of metrics in body
     */
    @GetMapping("/metrics")
    @Timed
    public List<Metric> getAllMetrics() {
        return metricService.findAll(null);
    }

    /**
     * GET  /metrics/:id : get the "id" metric.
     *
     * @param id the id of the metric to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the metric, or with status 404 (Not Found)
     */
    @GetMapping("/metrics/{id}")
    @Timed
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'METRIC.GET_LIST.ITEM')")
    public ResponseEntity<Metric> getMetric(@PathVariable Long id) {
        Metric metric = metricService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(metric));
    }

    /**
     * DELETE  /metrics/:id : delete the "id" metric.
     *
     * @param id the id of the metric to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/metrics/{id}")
    @Timed
    @PreAuthorize("hasPermission({'id': #id}, 'metric', 'METRIC.DELETE')")
    public ResponseEntity<Void> deleteMetric(@PathVariable Long id) {
        metricService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
