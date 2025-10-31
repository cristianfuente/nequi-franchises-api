package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.BranchCreateRequestDto;
import co.com.nequi.api.dto.BranchResponseDto;
import co.com.nequi.api.dto.BranchPageResponseDto;
import co.com.nequi.api.dto.PagedResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.usecase.branch.BranchUseCase;
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
import co.com.nequi.usecase.exception.ValidationException;

import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_CURSOR;
import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_LIMIT;

@Component
@RequiredArgsConstructor
@Tag(name = "Branches")
public class BranchHandler {

    private static final Logger log = LoggerFactory.getLogger(BranchHandler.class);

    private final BranchUseCase useCase;

    @Operation(
            summary = "Crear sucursal",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = BranchCreateRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creada",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> create(ServerRequest req) {
        String fid = req.pathVariable("fid");
        log.info("[Branches] create request franchiseId={}", fid);
        return req.bodyToMono(BranchCreateRequestDto.class)
                .flatMap(b -> useCase.createBranch(fid,
                        co.com.nequi.model.branch.Branch.builder().name(b.getName()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar sucursales por franquicia",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BranchPageResponseDto.class)))
    )
    public Mono<ServerResponse> getByFranchise(ServerRequest req) {
        String fid = req.pathVariable("fid");
        Integer limit = extractLimit(req);
        String cursor = extractCursor(req);
        log.info("[Branches] list request franchiseId={} limit={} cursor={}", fid, limit, cursor);
        return useCase.getByFranchiseId(fid, limit, cursor)
                .map(page -> PagedResponseDto.<BranchResponseDto>builder()
                        .items(page.getItems().stream().map(DtoMappers::toRes).toList())
                        .lastEvaluatedKey(page.getLastEvaluatedKey())
                        .build())
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    @Operation(
            summary = "Obtener sucursal por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> getById(ServerRequest req) {
        String bid = req.pathVariable("bid");
        log.info("[Branches] getById branchId={}", bid);
        return useCase.getById(bid)
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Actualizar nombre de sucursal",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RenameRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> updateName(ServerRequest req) {
        String bid = req.pathVariable("bid");
        log.info("[Branches] updateName branchId={}", bid);
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(bid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(summary = "Eliminar sucursal", responses = @ApiResponse(responseCode = "204", description = "Eliminada"))
    public Mono<ServerResponse> delete(ServerRequest req) {
        String bid = req.pathVariable("bid");
        log.info("[Branches] delete branchId={}", bid);
        return useCase.delete(bid).then(ServerResponse.noContent().build());
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
