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





/**
 * Criteria class for the Balance entity. This class is used in BalanceResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /balances?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class BalanceCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter key;

    private StringFilter typeKey;

    private StringFilter measureKey;

    private BigDecimalFilter amount;

    private BigDecimalFilter reserved;

    private LongFilter entityId;

    private StringFilter createdBy;

    public BalanceCriteria() {
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

    public StringFilter getMeasureKey() {
        return measureKey;
    }

    public void setMeasureKey(StringFilter measureKey) {
        this.measureKey = measureKey;
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

    public LongFilter getEntityId() {
        return entityId;
    }

    public void setEntityId(LongFilter entityId) {
        this.entityId = entityId;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "BalanceCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (key != null ? "key=" + key + ", " : "") +
                (typeKey != null ? "typeKey=" + typeKey + ", " : "") +
                (measureKey != null ? "measureKey=" + measureKey + ", " : "") +
                (amount != null ? "amount=" + amount + ", " : "") +
                (reserved != null ? "reserved=" + reserved + ", " : "") +
                (entityId != null ? "entityId=" + entityId + ", " : "") +
                (createdBy != null ? "createdBy=" + createdBy + ", " : "") +
            "}";
    }

}
