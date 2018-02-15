package com.icthh.xm.ms.balance.service.dto;


import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Balance entity.
 */
public class BalanceDTO implements Serializable {

    private Long id;

    @NotNull
    private String key;

    @NotNull
    private String typeKey;

    private String measureKey;

    private BigDecimal amount;

    private BigDecimal reserved;

    @NotNull
    private Long entityId;

    private String createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getMeasureKey() {
        return measureKey;
    }

    public void setMeasureKey(String measureKey) {
        this.measureKey = measureKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BalanceDTO balanceDTO = (BalanceDTO) o;
        if(balanceDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), balanceDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BalanceDTO{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", typeKey='" + getTypeKey() + "'" +
            ", measureKey='" + getMeasureKey() + "'" +
            ", amount=" + getAmount() +
            ", reserved=" + getReserved() +
            ", entityId=" + getEntityId() +
            ", createdBy='" + getCreatedBy() + "'" +
            "}";
    }
}
