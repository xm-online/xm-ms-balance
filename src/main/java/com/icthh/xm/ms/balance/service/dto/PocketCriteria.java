package com.icthh.xm.ms.balance.service.dto;

import java.io.Serializable;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.InstantFilter;




/**
 * Criteria class for the Pocket entity. This class is used in PocketResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /pockets?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PocketCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter key;

    private StringFilter typeKey;

    private InstantFilter startDateTime;

    private InstantFilter endDateTime;

    private BigDecimalFilter amount;

    private BigDecimalFilter reserved;

    private LongFilter balanceId;

    public PocketCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getKey() {
        return key;
    }

    public void setKey(StringFilter key) {
        this.key = key;
    }

    public StringFilter getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(StringFilter typeKey) {
        this.typeKey = typeKey;
    }

    public InstantFilter getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(InstantFilter startDateTime) {
        this.startDateTime = startDateTime;
    }

    public InstantFilter getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(InstantFilter endDateTime) {
        this.endDateTime = endDateTime;
    }

    public BigDecimalFilter getAmount() {
        return amount;
    }

    public void setAmount(BigDecimalFilter amount) {
        this.amount = amount;
    }

    public BigDecimalFilter getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimalFilter reserved) {
        this.reserved = reserved;
    }

    public LongFilter getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(LongFilter balanceId) {
        this.balanceId = balanceId;
    }

    @Override
    public String toString() {
        return "PocketCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (key != null ? "key=" + key + ", " : "") +
                (typeKey != null ? "typeKey=" + typeKey + ", " : "") +
                (startDateTime != null ? "startDateTime=" + startDateTime + ", " : "") +
                (endDateTime != null ? "endDateTime=" + endDateTime + ", " : "") +
                (amount != null ? "amount=" + amount + ", " : "") +
                (reserved != null ? "reserved=" + reserved + ", " : "") +
                (balanceId != null ? "balanceId=" + balanceId + ", " : "") +
            "}";
    }

}
