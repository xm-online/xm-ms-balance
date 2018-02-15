package com.icthh.xm.ms.balance.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * The balance materic structure to store value like maximum amount of the balance
 * denoted by this object due to all time of use.
 */
@ApiModel(description = "The balance materic structure to store value like maximum amount of the balance denoted by this object due to all time of use.")
@Entity
@Table(name = "metric")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Metric implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * This field is used to identify the metric.
     */
    @NotNull
    @ApiModelProperty(value = "This field is used to identify the metric.", required = true)
    @Column(name = "jhi_key", nullable = false)
    private String key;

    /**
     * String with the metric type identifer.
     */
    @NotNull
    @ApiModelProperty(value = "String with the metric type identifer.", required = true)
    @Column(name = "type_key", nullable = false)
    private String typeKey;

    /**
     * String with the metric value.
     */
    @ApiModelProperty(value = "String with the metric value.")
    @Column(name = "jhi_value")
    private String value;

    @ManyToOne(optional = false)
    @NotNull
    private Balance balance;

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

    public Metric key(String key) {
        this.key = key;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public Metric typeKey(String typeKey) {
        this.typeKey = typeKey;
        return this;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getValue() {
        return value;
    }

    public Metric value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Balance getBalance() {
        return balance;
    }

    public Metric balance(Balance balance) {
        this.balance = balance;
        return this;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
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
        Metric metric = (Metric) o;
        if (metric.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), metric.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Metric{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", typeKey='" + getTypeKey() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
