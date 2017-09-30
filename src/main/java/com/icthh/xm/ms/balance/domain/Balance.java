package com.icthh.xm.ms.balance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * A Balance.
 */
@Entity
@Table(name = "balance")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Balance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "jhi_key", nullable = false)
    private String key;

    @NotNull
    @Column(name = "type_key", nullable = false)
    private String typeKey;

    @Column(name = "measure_key")
    private String measureKey;

    @Column(name = "user_key")
    private String userKey;

    @Column(name = "amount", precision=10, scale=2)
    private BigDecimal amount;

    @Column(name = "reserved", precision=10, scale=2)
    private BigDecimal reserved;

    @OneToMany(mappedBy = "balance")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Pocket> pockets = new HashSet<>();

    @OneToMany(mappedBy = "balance")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Metric> metrics = new HashSet<>();

    // jhipster-needle-entity-add-field - Jhipster will add fields here, do not remove
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

    public String getUserKey() {
        return userKey;
    }

    public Balance userKey(String userKey) {
        this.userKey = userKey;
        return this;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Balance amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    // jhipster-needle-entity-add-getters-setters - Jhipster will add getters and setters here, do not remove

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
            ", userKey='" + getUserKey() + "'" +
            ", amount='" + getAmount() + "'" +
            ", reserved='" + getReserved() + "'" +
            "}";
    }
}
