package co.com.nequi.api.handler;

import co.com.nequi.api.DtoMappers;
import co.com.nequi.api.dto.FranchiseCreateRequestDto;
import co.com.nequi.api.dto.FranchiseResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.usecase.franchise.FranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase useCase;

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(FranchiseCreateRequestDto.class)
                .flatMap(body -> useCase.createFranchise(
                        co.com.nequi.model.franchise.Franchise.builder().name(body.getName()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> getById(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return useCase.getById(fid)
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> getAll(ServerRequest req) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(useCase.getAll().map(DtoMappers::toRes), FranchiseResponseDto.class);
    }

    public Mono<ServerResponse> updateName(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(fid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return useCase.delete(fid).then(ServerResponse.noContent().build());
    }

}
