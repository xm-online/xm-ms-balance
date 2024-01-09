package com.icthh.xm.ms.balance.service.dto;

import com.icthh.xm.ms.balance.service.OperationType;
import io.github.jhipster.service.filter.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Criteria class for the BalanceHistory entity. This class is used in BalanceHistoryResource to
 * receive all the possible filtering options from the Http GET request parameters.
 */
@Getter
@Setter
public class BalanceHistoryCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter balanceId;

    private LongFilter balanceEntityId;

    private Filter<OperationType> operationType;

    private StringFilter operationId;

    private BigDecimalFilter amountDelta;

    private BigDecimalFilter amountTotal;

    private InstantFilter operationDate;

    private InstantFilter entryDate;

    public BalanceHistoryCriteria() {
    }

    @Override
    public String toString() {
        return "BalanceHistoryCriteria{" +
            "balanceId=" + balanceId +
            ", balanceEntityId=" + balanceEntityId +
            ", operationType=" + operationType +
            ", operationId=" + operationId +
            ", amountDelta=" + amountDelta +
            ", amountTotal=" + amountTotal +
            ", operationDate=" + operationDate +
            ", entryDate=" + entryDate +
            '}';
    }
}
