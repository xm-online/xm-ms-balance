package com.icthh.xm.ms.balance.web.rest.errors;

import com.icthh.xm.commons.i18n.error.domain.vm.ErrorVM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Keeps the HTTP status carried by a {@link ResponseStatusException}.
 *
 * <p>{@code ResponseUtil.wrapOrNotFound} throws {@link ResponseStatusException} with 404 when an
 * entity is missing. The {@code ExceptionTranslator} from xm-commons has no handler for it, so it
 * falls into that translator's catch-all {@code processException(Exception)} and comes back as
 * <em>500 An unexpected error occurred: 404 NOT_FOUND</em>. Without this advice
 * {@code GET /api/balances/{unknownId}} — and the same call on metrics and pockets — answers 500
 * instead of 404.
 *
 * <p>This advice runs first and answers with the original status. The body follows the same
 * {@link ErrorVM} shape the rest of the API uses, so clients can parse errors uniformly rather
 * than meeting an empty body on these paths.
 *
 * <p>Drop this class once xm-commons handles {@link ResponseStatusException} itself.
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseStatusExceptionTranslator {

    private static final String ERROR_NOT_FOUND = "error.notFound";
    private static final String ERROR_HTTP_PREFIX = "error.http.";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorVM> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        log.debug("Translating ResponseStatusException to {}", ex.getStatusCode(), ex);

        return ResponseEntity.status(ex.getStatusCode())
            .headers(ex.getHeaders())
            .body(new ErrorVM(errorCode(status, ex), ex.getReason()));
    }

    private static String errorCode(HttpStatus status, ResponseStatusException ex) {
        if (status == HttpStatus.NOT_FOUND) {
            return ERROR_NOT_FOUND;
        }
        return ERROR_HTTP_PREFIX + ex.getStatusCode().value();
    }
}
