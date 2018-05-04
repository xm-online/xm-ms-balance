package com.icthh.xm.ms.balance.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * This structure describes the sub-balances called pockets. A pocket defines a
 * special lifetime for partial amount of the balance.
 */
@Slf4j
@ApiModel(description = "This structure describes the sub-balances called pockets. A pocket defines a special lifetime for partial amount of the balance.")
@Entity
@Table(name = "pocket")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Pocket implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /**
     * This field is used to identify the pocket.
     */
    @NotNull
    @ApiModelProperty(value = "This field is used to identify the pocket.", required = true)
    @Column(name = "jhi_key", nullable = false)
    private String key;

    /**
     * String with the pocket type identifer.
     */
    @NotNull
    @ApiModelProperty(value = "String with the pocket type identifer.", required = true)
    @Column(name = "label", nullable = false)
    private String label;

    /**
     * Date/DateTime when the pocket becomes valid (date included).
     */
    @ApiModelProperty(value = "Date/DateTime when the pocket becomes valid (date included).")
    @Column(name = "start_date_time")
    private Instant startDateTime;

    /**
     * Date/DateTime when the pocket becomes invalid (date excluded).
     */
    @ApiModelProperty(value = "Date/DateTime when the pocket becomes invalid (date excluded).")
    @Column(name = "end_date_time")
    private Instant endDateTime;

    /**
     * The value of the pocket denoted by this object.
     * The amount includes the reserved amount (see field reserved).
     */
    @ApiModelProperty(value = "The value of the pocket denoted by this object. The amount includes the reserved amount (see field reserved).")
    @Column(name = "amount", precision=10, scale=2)
    private BigDecimal amount;

    /**
     * The reserved amount from pocket for uncommitted reservation transactions.
     */
    @ApiModelProperty(value = "The reserved amount from pocket for uncommitted reservation transactions.")
    @Column(name = "reserved", precision=10, scale=2)
    private BigDecimal reserved;

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

    public Pocket key(String key) {
        this.key = key;
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public Pocket label(String label) {
        this.label = label;
        return this;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public Pocket startDateTime(Instant startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    public void setStartDateTime(Instant startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }

    public Pocket endDateTime(Instant endDateTime) {
        this.endDateTime = endDateTime;
        return this;
    }

    public void setEndDateTime(Instant endDateTime) {
        this.endDateTime = endDateTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Pocket amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public Pocket reserved(BigDecimal reserved) {
        this.reserved = reserved;
        return this;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public Balance getBalance() {
        return balance;
    }

    public Pocket balance(Balance balance) {
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
        Pocket pocket = (Pocket) o;
        if (pocket.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pocket.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Pocket{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", label='" + getLabel() + "'" +
            ", startDateTime='" + getStartDateTime() + "'" +
            ", endDateTime='" + getEndDateTime() + "'" +
            ", amount=" + getAmount() +
            ", reserved=" + getReserved() +
            "}";
    }

    public Pocket addMount(BigDecimal amount) {
        log.info("Add amount:{} to pocket {}", amount, this);
        this.amount.add(amount);
        return this;
    }
}
