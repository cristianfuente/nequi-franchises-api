package co.com.nequi.usecase.util;

import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import reactor.core.publisher.Mono;

public final class ReactorChecks {

    private ReactorChecks() {
    }

    public static Mono<Void> validateNotEmptyValue(String value,
                                                   ExceptionMessage exceptionMessage) {
        return Mono.defer(() ->
                (value == null || value.trim().isEmpty())
                        ? Mono.error(new ValidationException(exceptionMessage))
                        : Mono.empty()
        );
    }

    public static <T> Mono<T> notFoundIfEmpty(Mono<T> mono, ExceptionMessage message) {
        return mono.switchIfEmpty(Mono.error(new ResourceNotFoundException(message)));
    }

}
