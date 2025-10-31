package co.com.nequi.api.handler;

import co.com.nequi.api.dto.ErrorDto;
import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.FranchiseCreateRequestDto;
import co.com.nequi.api.dto.FranchiseResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.usecase.franchise.FranchiseUseCase;
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
@Tag(name = "Franchises")
public class FranchiseHandler {

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
        return useCase.getById(fid)
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(
            summary = "Listar franquicias",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FranchiseResponseDto.class))))
    )
    public Mono<ServerResponse> getAll(ServerRequest req) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(useCase.getAll().map(DtoMappers::toRes), FranchiseResponseDto.class);
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
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(fid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    @Operation(summary = "Eliminar franquicia", responses = @ApiResponse(responseCode = "204", description = "Eliminada"))
    public Mono<ServerResponse> delete(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return useCase.delete(fid).then(ServerResponse.noContent().build());
    }

}
