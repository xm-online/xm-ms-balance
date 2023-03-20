package com.icthh.xm.ms.balance.service;

import com.icthh.xm.ms.balance.service.FilterConverter.QueryPart;
import com.icthh.xm.ms.balance.service.dto.BalanceCriteria;
import com.icthh.xm.ms.balance.service.dto.PocketCriteria;
import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringRunner.class)
@Slf4j
public class FilterConverterUnitTest {

    @Test
    public void testThreeExpressions() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setEquals(42L));
        criteria.setAmount((BigDecimalFilter) new BigDecimalFilter().setEquals(BigDecimal.ONE));
        criteria.setStatus((StringFilter) new StringFilter().setEquals("ACTIVE"));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("amount = :amount and id = :id and status = :status", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));
        assertEquals(BigDecimal.ONE, queryPart.getParams().get("amount"));
        assertEquals("ACTIVE", queryPart.getParams().get("status"));
    }

    @Test
    public void testSpecifiedTrueExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setSpecified(true));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id is not null ", queryPart.getQuery().toString());
        assertTrue(queryPart.getParams().isEmpty());
    }

    @Test
    public void testSpecifiedFalseExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setSpecified(false));
        QueryPart queryPart = createQueryPart(criteria);

        System.out.println(queryPart);

        assertEquals("id is null ", queryPart.getQuery().toString());
        assertTrue(queryPart.getParams().isEmpty());

    }

    @Test
    public void testEqualsExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setEquals(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id = :id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));

    }

    @Test
    public void testGreaterThanExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setGreaterThan(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id > :id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));
    }

    @Test
    public void testLessThanExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setLessThan(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id < :id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));
    }

    @Test
    public void testGreaterOrEqualThanExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setGreaterOrEqualThan(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id >= :id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));
    }

    @Test
    public void testLessOrEqualThanExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setLessOrEqualThan(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id <= :id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("id"));
    }

    @Test
    public void testInExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setId((LongFilter) new LongFilter().setIn(Arrays.asList(1L, 2L)));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("id in :id", queryPart.getQuery().toString());
        assertArrayEquals(Arrays.asList(1L, 2L).toArray(), ((List) queryPart.getParams().get("id")).toArray());
    }

    @Test
    public void testContainsExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setKey(new StringFilter().setContains("thing"));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("key like :key", queryPart.getQuery().toString());
        assertEquals("thing", queryPart.getParams().get("key"));
    }

    @Test
    public void testComplexExpression() {
        BalanceCriteria criteria = new BalanceCriteria();
        criteria.setAmount((BigDecimalFilter) new BigDecimalFilter().setGreaterThan(BigDecimal.ONE)
                                                                    .setLessThan(BigDecimal.TEN));
        criteria.setKey(new StringFilter().setContains("key_"));
        criteria.setTypeKey((StringFilter) new StringFilter().setIn(Arrays.asList("1", "2", "3")));
        criteria.setMeasureKey((StringFilter) new StringFilter().setEquals("measureKey_"));

        QueryPart queryPart = createQueryPart(criteria);

        assertEquals(
            "measureKey = :measureKey and amount > :amount and amount < :amount1 "
            + "and typeKey in :typeKey and key like :key",
            queryPart.getQuery().toString());
        assertEquals(BigDecimal.ONE, queryPart.getParams().get("amount"));
        assertEquals(BigDecimal.TEN, queryPart.getParams().get("amount1"));
        assertEquals("key_", queryPart.getParams().get("key"));
        assertArrayEquals(Arrays.asList("1", "2", "3").toArray(),
                          ((List) queryPart.getParams().get("typeKey")).toArray());
        assertEquals("measureKey_", queryPart.getParams().get("measureKey"));

    }

    @Test
    public void testForeignKeyColumnConversion() {
        PocketCriteria criteria = new PocketCriteria();
        criteria.setBalanceId((LongFilter) new LongFilter().setEquals(42L));
        QueryPart queryPart = createQueryPart(criteria);

        assertEquals("balance_id = :balance_id", queryPart.getQuery().toString());
        assertEquals(42L, queryPart.getParams().get("balance_id"));
    }

    private QueryPart createQueryPart(final Object criteria) {
        return FilterConverter.toJpql(criteria);
    }

}
