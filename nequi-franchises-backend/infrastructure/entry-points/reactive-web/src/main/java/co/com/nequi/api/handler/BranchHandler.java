package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.BranchCreateRequestDto;
import co.com.nequi.api.dto.BranchResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.usecase.branch.BranchUseCase;
import io.swagger.v3.oas.annotations.Operation;
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
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Tag(name = "Branches")
public class BranchHandler {

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
        return req.bodyToMono(BranchCreateRequestDto.class)
                .flatMap(b -> useCase.createBranch(fid,
                        co.com.nequi.model.branch.Branch.builder().name(b.getName()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar sucursales por franquicia",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BranchResponseDto.class))))
    )
    public Mono<ServerResponse> getByFranchise(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(useCase.getByFranchiseId(fid).map(DtoMappers::toRes), BranchResponseDto.class);
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
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(bid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(summary = "Eliminar sucursal", responses = @ApiResponse(responseCode = "204", description = "Eliminada"))
    public Mono<ServerResponse> delete(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return useCase.delete(bid).then(ServerResponse.noContent().build());
    }

}
