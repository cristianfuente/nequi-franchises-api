package co.com.nequi.api.mapper;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.usecase.exception.BusinessException;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ErrorHttpMapper {

    public Mono<ServerResponse> toResponse(Throwable ex) {
        HttpStatus status;
        String code;
        String message;

        if (ex instanceof ValidationException validationException) {
            status = HttpStatus.BAD_REQUEST;
            code = validationException.getCode();
            message = validationException.getMessage();
        } else if (ex instanceof ResourceNotFoundException resourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            code = resourceNotFoundException.getCode();
            message = resourceNotFoundException.getMessage();
        } else if (ex instanceof BusinessException businessException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            code = businessException.getCode();
            message = businessException.getMessage();
        } else if (ex instanceof DecodingException || ex instanceof ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            code = "BAD_REQUEST";
            message = "Solicitud inválida. Verifique el cuerpo y parámetros.";
        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = status.is4xxClientError() ? "BAD_REQUEST" : "ERROR";
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "INTERNAL_ERROR";
            message = "Ocurrió un error inesperado. Intenta nuevamente.";
        }

        var dto = ErrorDto.builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto);
    }

}
