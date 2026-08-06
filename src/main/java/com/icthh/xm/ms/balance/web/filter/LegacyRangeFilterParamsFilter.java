package com.icthh.xm.ms.balance.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Accepts the range filter query parameter names used before the jhipster-framework 9 upgrade.
 *
 * <p>jhipster renamed the {@code RangeFilter} properties:
 * <pre>
 *   greaterOrEqualThan -&gt; greaterThanOrEqual
 *   lessOrEqualThan    -&gt; lessThanOrEqual
 * </pre>
 *
 * <p>Criteria objects are bound from query parameters by property name, so the rename silently
 * changes the public API of every criteria endpoint ({@code /api/balances}, {@code /api/pockets},
 * {@code /api/metrics}, {@code /api/v2/balances/history}). An unknown nested property is not an
 * error for Spring — it is dropped — so a client still sending the old spelling gets HTTP 200 with
 * the filter never applied, i.e. a wider result set than it asked for, with nothing to indicate it.
 *
 * <p>This filter renames the parameters before binding. It deliberately does not touch the criteria
 * classes: their setters are called by tenant LEP scripts, and narrowing a setter parameter type
 * would break those scripts at runtime.
 *
 * <p>Remove once no client uses the old names.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LegacyRangeFilterParamsFilter extends OncePerRequestFilter {

    private static final String LEGACY_GREATER = ".greaterOrEqualThan";
    private static final String CURRENT_GREATER = ".greaterThanOrEqual";
    private static final String LEGACY_LESS = ".lessOrEqualThan";
    private static final String CURRENT_LESS = ".lessThanOrEqual";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (hasLegacyParam(request)) {
            chain.doFilter(new LegacyParamsRequest(request), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean hasLegacyParam(HttpServletRequest request) {
        for (String name : Collections.list(request.getParameterNames())) {
            if (isLegacy(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLegacy(String name) {
        return name.endsWith(LEGACY_GREATER) || name.endsWith(LEGACY_LESS);
    }

    private static String rename(String name) {
        if (name.endsWith(LEGACY_GREATER)) {
            return name.substring(0, name.length() - LEGACY_GREATER.length()) + CURRENT_GREATER;
        }
        if (name.endsWith(LEGACY_LESS)) {
            return name.substring(0, name.length() - LEGACY_LESS.length()) + CURRENT_LESS;
        }
        return name;
    }

    private static final class LegacyParamsRequest extends HttpServletRequestWrapper {

        private final Map<String, String[]> parameters;

        private LegacyParamsRequest(HttpServletRequest request) {
            super(request);
            Map<String, String[]> renamed = new LinkedHashMap<>();
            request.getParameterMap().forEach((name, values) -> {
                String target = rename(name);
                if (!target.equals(name)) {
                    log.debug("Deprecated range filter parameter '{}', reading it as '{}'", name, target);
                }
                // an explicit current-name parameter wins over the legacy alias
                renamed.merge(target, values, (existing, ignored) -> existing);
            });
            this.parameters = Collections.unmodifiableMap(renamed);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = parameters.get(name);
            return values == null ? null : values.clone();
        }

        @Override
        public String getParameter(String name) {
            String[] values = parameters.get(name);
            return values == null || values.length == 0 ? null : values[0];
        }
    }
}
