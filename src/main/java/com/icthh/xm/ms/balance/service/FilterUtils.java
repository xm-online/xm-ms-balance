package com.icthh.xm.ms.balance.service;

import io.github.jhipster.service.filter.Filter;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to cast  <code>Filter.class</code> fields string value to specified enum type.
 * Since jhipster does not have a filter implementation for the Enum type, the default <code>Filter.class</code>
 * reads the filter value as a string, as a result of which we cannot apply filter to the entity with Enum fields,
 * because we got ClassCastException when executing the request.
 */
@UtilityClass
public class FilterUtils {

    public static <T extends Enum<T>> void remapEnumFilter(Filter<T> filter, Class<T> enumType) {
        if (filter.getEquals() != null) {
            String equals = String.valueOf(filter.getEquals());
            filter.setEquals(Enum.valueOf(enumType, equals));
        }

        if (filter.getNotEquals() != null) {
            String notEquals = String.valueOf(filter.getNotEquals());
            filter.setNotEquals(Enum.valueOf(enumType, notEquals));
        }

        if (filter.getIn() != null) {
            List<T> newInFileterList = new ArrayList<>();
            for (int i = 0; i < filter.getIn().size(); i++) {
                String in = String.valueOf(filter.getIn().get(i));
                newInFileterList.add(Enum.valueOf(enumType, in));
            }
            filter.setIn(newInFileterList);
        }
    }
}
