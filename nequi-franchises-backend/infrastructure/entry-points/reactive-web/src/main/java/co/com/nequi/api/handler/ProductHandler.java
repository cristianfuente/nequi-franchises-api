package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.ChangeStockRequestDto;
import co.com.nequi.api.dto.ProductCreateRequestDto;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.api.mapper.DtoMappers.toSummary;

@Component
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductHandler {

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
                .flatMap(b -> productUseCase.createProduct(bid,
                        co.com.nequi.model.product.Product.builder()
                                .name(b.getName()).stock(b.getStock()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar productos por sucursal",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponseDto.class))))
    )
    public Mono<ServerResponse> listByBranch(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productUseCase.getByBranchId(bid).map(DtoMappers::toRes), ProductResponseDto.class);
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
        Flux<TopProductItemResponseDto> items =
                branchUseCase.getByFranchiseId(fid)
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

}
