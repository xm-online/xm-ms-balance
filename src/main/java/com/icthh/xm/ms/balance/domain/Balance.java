package com.icthh.xm.ms.balance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * This structure describes the balance definition that came out of the billing systems.
 */
@Getter
@Setter
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
     * The value of the balance denoted by this object.
     * The amount includes the reserved amount (see field reserved).
     */
    @ApiModelProperty(value = "The value of the balance denoted by this object. The amount includes the reserved amount (see field reserved).")
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The reserved amount from the balance for uncommitted reservation transactions.
     */
    @ApiModelProperty(value = "The reserved amount from the balance for uncommitted reservation transactions.")
    @Column(name = "reserved", precision = 10, scale = 2)
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
     * Balance status.
     */
    @ApiModelProperty(value = "Balance status.")
    @Column(name = "status")
    private String status;

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

    public Balance key(String key) {
        this.key = key;
        return this;
    }

    public Balance typeKey(String typeKey) {
        this.typeKey = typeKey;
        return this;
    }

    public Balance measureKey(String measureKey) {
        this.measureKey = measureKey;
        return this;
    }

    public Balance reserved(BigDecimal reserved) {
        this.reserved = reserved;
        return this;
    }

    public Balance entityId(Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public Balance amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Balance createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Balance status(String status) {
        this.status = status;
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Balance balance = (Balance) o;
        return balance.getId() != null && getId() != null && Objects.equals(getId(), balance.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Balance{"
            + "id=" + getId()
            + ", key='" + getKey() + "'"
            + ", typeKey='" + getTypeKey() + "'"
            + ", measureKey='" + getMeasureKey() + "'"
            + ", amount=" + getAmount()
            + ", reserved=" + getReserved()
            + ", entityId=" + getEntityId()
            + ", createdBy='" + getCreatedBy() + "'"
            + "}";
    }

}
