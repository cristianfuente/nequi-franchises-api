package co.com.nequi.api.config;

import co.com.nequi.api.mapper.ErrorHttpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ErrorFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(ErrorFilterConfig.class);

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> errorFilter(ErrorHttpMapper mapper) {
        return (request, next) -> next.handle(request)
                .doOnSubscribe(ignored ->
                        log.debug("Handling {} {}", request.methodName(), request.path()))
                .doOnNext(response ->
                        log.debug("Completed {} {} -> {}", request.methodName(), request.path(), response.statusCode()))
                .onErrorResume(ex -> {
                    log.error("Unhandled exception processing {} {}: {}", request.methodName(), request.path(), ex.getMessage(), ex);
                    return mapper.toResponse(ex)
                            .doOnNext(errorResponse ->
                                    log.debug("Mapped error response for {} {} -> {}", request.methodName(), request.path(), errorResponse.statusCode()));
                });
    }
}
