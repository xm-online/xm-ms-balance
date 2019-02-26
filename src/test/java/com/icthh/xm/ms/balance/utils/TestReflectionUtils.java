package com.icthh.xm.ms.balance.utils;

import lombok.experimental.UtilityClass;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;

import static java.time.Clock.systemDefaultZone;

@UtilityClass
public final class TestReflectionUtils {
    private static final String CLOCK_FIELD_NAME = "clock";

    public static Clock setClock(Object targetObject, long epochMillis) {
        return setClock(targetObject, epochMillis, CLOCK_FIELD_NAME);
    }

    public static Clock setClock(Object targetObject, long epochMillis, String clockFieldName) {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(epochMillis), systemDefaultZone().getZone());
        ReflectionTestUtils.setField(targetObject, clockFieldName, clock);
        return clock;
    }
}
