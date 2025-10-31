package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.ChangeStockRequestDto;
import co.com.nequi.api.dto.ProductCreateRequestDto;
import co.com.nequi.api.dto.PagedResponseDto;
import co.com.nequi.api.dto.ProductPageResponseDto;
import co.com.nequi.api.dto.ProductResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.api.dto.TopProductItemResponseDto;
import co.com.nequi.usecase.branch.BranchUseCase;
import co.com.nequi.usecase.product.ProductUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import co.com.nequi.usecase.exception.ValidationException;

import static co.com.nequi.api.mapper.DtoMappers.toSummary;
import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_CURSOR;
import static co.com.nequi.usecase.constant.ExceptionMessage.INVALID_PAGINATION_LIMIT;

@Component
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductHandler.class);

    private final ProductUseCase productUseCase;
    private final BranchUseCase branchUseCase;

    @Operation(
            summary = "Crear producto en sucursal",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ProductCreateRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "Sucursal/Franquicia no encontrada",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> create(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return req.bodyToMono(ProductCreateRequestDto.class)
                .doOnNext(body -> log.info("[Products] create request branchId={} name={} stock={}", bid, body.getName(), body.getStock()))
                .flatMap(b -> productUseCase.createProduct(bid,
                        co.com.nequi.model.product.Product.builder()
                                .name(b.getName()).stock(b.getStock()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar productos por sucursal",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductPageResponseDto.class)))
    )
    public Mono<ServerResponse> listByBranch(ServerRequest req) {
        String bid = req.pathVariable("bid");
        Integer limit = extractLimit(req);
        String cursor = extractCursor(req);
        log.info("[Products] list request branchId={} limit={} cursor={}", bid, limit, cursor);
        return productUseCase.getByBranchId(bid, limit, cursor)
                .map(page -> PagedResponseDto.<ProductResponseDto>builder()
                        .items(page.getItems().stream().map(DtoMappers::toRes).toList())
                        .lastEvaluatedKey(page.getLastEvaluatedKey())
                        .build())
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    @Operation(
            summary = "Buscar productos por prefijo de nombre",
            parameters = @Parameter(name = "prefix", in = ParameterIn.QUERY, required = true,
                    description = "Prefijo de búsqueda (no sensible a mayúsculas)"),
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductPageResponseDto.class)))
    )
    public Mono<ServerResponse> searchByName(ServerRequest req) {
        String bid = req.pathVariable("bid");
        Integer limit = extractLimit(req);
        String cursor = extractCursor(req);
        String prefix = req.queryParam("prefix").orElse("");
        log.info("[Products] search request branchId={} prefix='{}' limit={} cursor={}", bid, prefix, limit, cursor);
        return productUseCase.searchByName(bid, prefix, limit, cursor)
                .map(page -> PagedResponseDto.<ProductResponseDto>builder()
                        .items(page.getItems().stream().map(DtoMappers::toRes).toList())
                        .lastEvaluatedKey(page.getLastEvaluatedKey())
                        .build())
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    @Operation(
            summary = "Actualizar nombre de producto",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = RenameRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> updateName(ServerRequest req) {
        String pid = req.pathVariable("pid");
        return req.bodyToMono(RenameRequestDto.class)
                .doOnNext(body -> log.info("[Products] updateName productId={} newName={}", pid, body.getName()))
                .flatMap(b -> productUseCase.updateName(pid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Cambiar stock por delta",
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Clave de idempotencia para reintentos seguros"),
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ChangeStockRequestDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> changeStock(ServerRequest req) {
        String pid = req.pathVariable("pid");
        String idem = req.headers().firstHeader("Idempotency-Key");
        return req.bodyToMono(ChangeStockRequestDto.class)
                .doOnNext(body -> log.info("[Products] changeStock productId={} delta={} idempotencyKey={}", pid, body.getDelta(), idem))
                .flatMap(b -> productUseCase.changeStock(pid, b.getDelta(), idem))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(summary = "Eliminar producto de una sucursal",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Eliminado"),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    public Mono<ServerResponse> deleteByBranch(ServerRequest req) {
        String bid = req.pathVariable("bid");
        String pid = req.pathVariable("pid");
        log.info("[Products] delete productId={} branchId={}", pid, bid);
        return productUseCase.deleteByBranch(bid, pid)
                .then(ServerResponse.noContent().build());
    }

    @Operation(
            summary = "Top producto por sucursal de una franquicia",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopProductItemResponseDto.class))))
    )
    public Mono<ServerResponse> topByFranchise(ServerRequest req) {
        String fid = req.pathVariable("fid");
        log.info("[Products] top products request franchiseId={}", fid);
        Flux<TopProductItemResponseDto> items =
                branchUseCase.streamByFranchiseId(fid)
                        .flatMap(branch ->
                                        productUseCase.getMaxStockByBranch(branch.getId())
                                                .map(p -> TopProductItemResponseDto.builder()
                                                        .branchId(branch.getId())
                                                        .branchName(branch.getName())
                                                        .product(toSummary(p))
                                                        .build())
                                                .onErrorResume(ex -> Mono.empty()) // si una sucursal no tiene productos, la omitimos
                                , 16);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(items.collectList(), TopProductItemResponseDto.class);
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
