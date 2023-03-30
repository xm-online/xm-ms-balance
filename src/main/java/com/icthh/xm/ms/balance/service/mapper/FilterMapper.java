package com.icthh.xm.ms.balance.service.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;
import io.github.jhipster.service.filter.Filter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FilterMapper {

    public <E extends Enum<E>> Filter<E> toEnumFilter(Filter<String> stringFilter, Class<E> enumType) {
        Filter<E> enumFilter = new Filter<>();
        Optional<Filter<String>> stringFilterOpt = Optional.ofNullable(stringFilter);

        mapFieldToFilter(enumType, stringFilterOpt, Filter::getEquals, enumFilter::setEquals);
        mapFieldToFilter(enumType, stringFilterOpt, Filter::getNotEquals, enumFilter::setNotEquals);
        mapFieldsToFilter(enumType, stringFilterOpt, enumFilter::setIn, Filter::getIn);

        stringFilterOpt.map(Filter::getSpecified)
            .ifPresent(enumFilter::setSpecified);

        return enumFilter;
    }

    private <E extends Enum<E>> void mapFieldsToFilter(Class<E> enumType,
                                                       Optional<Filter<String>> stringFilterOpt,
                                                       Consumer<List<E>> fieldSetter,
                                                       Function<Filter<String>, List<String>> fieldExtractor) {

        List<E> enums = stringFilterOpt.map(fieldExtractor)
            .stream()
            .flatMap(Collection::stream)
            .map(enumValue -> parseEnum(enumType, enumValue))
            .collect(Collectors.toList());
        if (!enums.isEmpty()) {
            fieldSetter.accept(enums);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String enumValue) {
        E parsedEnum;
        try {
            parsedEnum = Enum.valueOf(enumType, enumValue.toUpperCase().replace("-", "_"));
        } catch (Exception e) {
            log.error("Can not parse enum with type {} from value {}", enumType.getSimpleName(), enumValue, e);
            throw new BusinessException(ErrorConstants.ERR_VALIDATION,
                String.format("Not valid enum value %s for type %s", enumValue, enumType.getSimpleName()));
        }
        return parsedEnum;
    }

    private <E extends Enum<E>> void mapFieldToFilter(Class<E> enumType,
                                                      Optional<Filter<String>> stringFilterOpt,
                                                      Function<Filter<String>, String> fieldExtractor,
                                                      Consumer<E> fieldSetter) {

        stringFilterOpt.map(fieldExtractor)
            .map(enumValue -> parseEnum(enumType, enumValue))
            .ifPresent(fieldSetter);
    }
}
