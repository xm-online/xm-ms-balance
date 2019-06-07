package com.icthh.xm.ms.balance.service.dto;


import java.util.Map;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lombok.Data;

/**
 * A DTO for the Pocket entity.
 */
@Data
public class PocketDTO implements Serializable {

    private Long id;

    @NotNull
    private String key;

    @NotNull
    private String label;

    private Instant startDateTime;

    private Instant endDateTime;

    private BigDecimal amount;

    private BigDecimal reserved;

    private Long balanceId;

    private Map<String, String> metadata;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PocketDTO pocketDTO = (PocketDTO) o;
        if(pocketDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), pocketDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PocketDTO{" +
            "id=" + getId() +
            ", key='" + getKey() + "'" +
            ", label='" + getLabel() + "'" +
            ", startDateTime='" + getStartDateTime() + "'" +
            ", endDateTime='" + getEndDateTime() + "'" +
            ", amount=" + getAmount() +
            ", reserved=" + getReserved() +
            "}";
    }
}
