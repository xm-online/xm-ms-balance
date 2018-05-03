package com.icthh.xm.ms.balance.service.dto;


import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Balance entity.
 */
@Getter
@Setter
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

    public BalanceDTO amount(BigDecimal amount) {
        this.amount = amount;
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
