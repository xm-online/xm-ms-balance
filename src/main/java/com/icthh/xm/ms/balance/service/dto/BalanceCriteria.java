package com.icthh.xm.ms.balance.service.dto;

import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * Criteria class for the Balance entity. This class is used in BalanceResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /balances?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
public class BalanceCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter key;

    private StringFilter typeKey;

    private StringFilter measureKey;

    private BigDecimalFilter reserved;

    private BigDecimalFilter amount;

    private LongFilter entityId;

    private StringFilter createdBy;

    public BalanceCriteria() {
    }

    @Override
    public String toString() {
        return "BalanceCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (key != null ? "key=" + key + ", " : "") +
                (typeKey != null ? "typeKey=" + typeKey + ", " : "") +
                (measureKey != null ? "measureKey=" + measureKey + ", " : "") +
                (reserved != null ? "reserved=" + reserved + ", " : "") +
                (entityId != null ? "entityId=" + entityId + ", " : "") +
                (createdBy != null ? "createdBy=" + createdBy + ", " : "") +
            "}";
    }

}
