package co.com.nequi.usecase.util;

import co.com.nequi.usecase.constant.ExceptionMessage;
import reactor.core.publisher.Mono;

public final class ReactorChecks {

    private ReactorChecks() {}
    public static <T> Mono<T> notFoundIfEmpty(Mono<T> mono, ExceptionMessage message) {
        return mono.switchIfEmpty(Mono.error(new co.com.nequi.usecase.exception.ResourceNotFoundException(message)));
    }

}
