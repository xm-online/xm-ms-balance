package com.icthh.xm.ms.balance.service.dto;

import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

import java.io.Serializable;


/**
 * Criteria class for the Metric entity. This class is used in MetricResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /metrics?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class MetricCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter key;

    private StringFilter typeKey;

    private StringFilter value;

    private LongFilter balanceId;

    public MetricCriteria() {
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

    public StringFilter getValue() {
        return value;
    }

    public void setValue(StringFilter value) {
        this.value = value;
    }

    public LongFilter getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(LongFilter balanceId) {
        this.balanceId = balanceId;
    }

    @Override
    public String toString() {
        return "MetricCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (key != null ? "key=" + key + ", " : "") +
                (typeKey != null ? "typeKey=" + typeKey + ", " : "") +
                (value != null ? "value=" + value + ", " : "") +
                (balanceId != null ? "balanceId=" + balanceId + ", " : "") +
            "}";
    }

}
