package co.com.nequi.api.config;

import co.com.nequi.api.mapper.ErrorHttpMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ErrorFilterConfig {
    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> errorFilter(ErrorHttpMapper mapper) {
        return (request, next) -> next.handle(request)
                .onErrorResume(mapper::toResponse);
    }
}
