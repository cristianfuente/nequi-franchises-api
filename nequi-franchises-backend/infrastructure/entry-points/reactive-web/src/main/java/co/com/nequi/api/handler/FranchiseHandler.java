package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.dto.FranchiseCreateRequestDto;
import co.com.nequi.api.dto.FranchisePageResponseDto;
import co.com.nequi.api.dto.FranchiseResponseDto;
import co.com.nequi.api.dto.PagedResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.usecase.exception.ValidationException;
import co.com.nequi.usecase.franchise.FranchiseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_CURSOR;
import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_LIMIT;

@Component
@RequiredArgsConstructor
@Tag(name = "Franchises")
public class FranchiseHandler {

    private static final Logger log = LoggerFactory.getLogger(FranchiseHandler.class);

    private final FranchiseUseCase useCase;

    @Operation(
            summary = "Crear franquicia",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = FranchiseCreateRequestDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creada",
                            content = @Content(schema = @Schema(implementation = FranchiseResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> create(ServerRequest req) {
        log.info("[Franchises] create request received");
        return req.bodyToMono(FranchiseCreateRequestDto.class)
                .flatMap(body -> useCase.createFranchise(
                        co.com.nequi.model.franchise.Franchise.builder().name(body.getName()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }


    @Operation(
            summary = "Obtener franquicia por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = FranchiseResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> getById(ServerRequest req) {
        String fid = req.pathVariable("fid");
        log.info("[Franchises] getById fid={}", fid);
        return useCase.getById(fid)
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar franquicias",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FranchisePageResponseDto.class)))
    )
    public Mono<ServerResponse> getAll(ServerRequest req) {
        Integer limit = extractLimit(req);
        String cursor = extractCursor(req);
        log.info("[Franchises] list request limit={} cursor={}", limit, cursor);
        return useCase.getAll(limit, cursor)
                .map(page -> PagedResponseDto.<FranchiseResponseDto>builder()
                        .items(page.getItems().stream().map(DtoMappers::toRes).toList())
                        .lastEvaluatedKey(page.getLastEvaluatedKey())
                        .build())
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    @Operation(
            summary = "Actualizar nombre de franquicia",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RenameRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = FranchiseResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> updateName(ServerRequest req) {
        String fid = req.pathVariable("fid");
        log.info("[Franchises] updateName fid={}", fid);
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(fid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(summary = "Eliminar franquicia", responses = @ApiResponse(responseCode = "204", description = "Eliminada"))
    public Mono<ServerResponse> delete(ServerRequest req) {
        String fid = req.pathVariable("fid");
        log.info("[Franchises] delete fid={}", fid);
        return useCase.delete(fid).then(ServerResponse.noContent().build());
    }

    private Integer extractLimit(ServerRequest req) {
        return req.queryParam("limit")
                .map(value -> {
                    try {
                        int parsed = Integer.parseInt(value);
                        if (parsed < 0) {
                            throw new ValidationException(INVALID_PAGINATION_LIMIT);
                        }
                        return parsed;
                    } catch (NumberFormatException ex) {
                        throw new ValidationException(INVALID_PAGINATION_LIMIT);
                    }
                })
                .orElse(null);
    }

    private String extractCursor(ServerRequest req) {
        return req.queryParam("cursor")
                .map(cursor -> {
                    if (cursor.isBlank()) {
                        throw new ValidationException(INVALID_PAGINATION_CURSOR);
                    }
                    return cursor;
                })
                .orElse(null);
    }

}
