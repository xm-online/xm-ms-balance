package com.icthh.xm.ms.balance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * This structure describes the balance definition that came out of the billing systems.
 */
@ApiModel(description = "This structure describes the balance definition that came out of the billing systems.")
@Entity
@Table(name = "balance")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Balance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * This field is used to identify the balance.
     */
    @NotNull
    @ApiModelProperty(value = "This field is used to identify the balance.", required = true)
    @Column(name = "jhi_key", nullable = false)
    private String key;

    /**
     * This field is used to identify the balance type (e.g. `MAIN`, `BONUS`, `DEBT`, ect.)
     */
    @NotNull
    @ApiModelProperty(value = "This field is used to identify the balance type (e.g. `MAIN`, `BONUS`, `DEBT`, ect.)", required = true)
    @Column(name = "type_key", nullable = false)
    private String typeKey;

    /**
     * The measure of the specified balance (e.g. `EUR`, `USD`, `watt`, `byte`, number of SMS, etc.)
     */
    @ApiModelProperty(value = "The measure of the specified balance (e.g. `EUR`, `USD`, `watt`, `byte`, number of SMS, etc.)")
    @Column(name = "measure_key")
    private String measureKey;

    /**
     * The reserved amount from the balance for uncommitted reservation transactions.
     */
    @ApiModelProperty(value = "The reserved amount from the balance for uncommitted reservation transactions.")
    @Column(name = "reserved", precision=10, scale=2)
    private BigDecimal reserved;

    /**
     * The Entity ID related to this balance.
     */
    @NotNull
    @ApiModelProperty(value = "The Entity ID related to this balance.", required = true)
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Created by user key.
     */
    @ApiModelProperty(value = "Created by user key.")
    @Column(name = "created_by")
    private String createdBy;

    /**
     * List of pockets owned by the balance. A null value indicates balances that cannot
     * have pockets because they are not configured in the specification. An empty list
     * indicates that are no pockets defined.
     */
    @ApiModelProperty(value = "List of pockets owned by the balance. A null value indicates balances that cannot have pockets because they are not configured in the specification. An empty list indicates that are no pockets defined.")
    @OneToMany(mappedBy = "balance")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Pocket> pockets = new HashSet<>();

    /**
     * List of metrics owned by the balance.
     */
    @ApiModelProperty(value = "List of metrics owned by the balance.")
    @OneToMany(mappedBy = "balance")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Metric> metrics = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public Balance key(String key) {
        this.key = key;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public Balance typeKey(String typeKey) {
        this.typeKey = typeKey;
        return this;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getMeasureKey() {
        return measureKey;
    }

    public Balance measureKey(String measureKey) {
        this.measureKey = measureKey;
        return this;
    }

    public void setMeasureKey(String measureKey) {
        this.measureKey = measureKey;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public Balance reserved(BigDecimal reserved) {
        this.reserved = reserved;
        return this;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Balance entityId(Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Balance createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Set<Pocket> getPockets() {
        return pockets;
    }

    public Balance pockets(Set<Pocket> pockets) {
        this.pockets = pockets;
        return this;
    }

    public Balance addPockets(Pocket pocket) {
        this.pockets.add(pocket);
        pocket.setBalance(this);
        return this;
    }

    public Balance removePockets(Pocket pocket) {
        this.pockets.remove(pocket);
        pocket.setBalance(null);
        return this;
    }

    public void setPockets(Set<Pocket> pockets) {
        this.pockets = pockets;
    }

    public Set<Metric> getMetrics() {
        return metrics;
    }

    public Balance metrics(Set<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public Balance addMetrics(Metric metric) {
        this.metrics.add(metric);
        metric.setBalance(this);
        return this;
    }

    public Balance removeMetrics(Metric metric) {
        this.metrics.remove(metric);
        metric.setBalance(null);
        return this;
    }

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Balance balance = (Balance) o;
        if (balance.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), balance.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Balance{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", typeKey='" + getTypeKey() + "'" +
            ", measureKey='" + getMeasureKey() + "'" +
            ", reserved=" + getReserved() +
            ", entityId=" + getEntityId() +
            ", createdBy='" + getCreatedBy() + "'" +
            "}";
    }
}
