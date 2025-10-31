package co.com.nequi.api.handler;

import co.com.nequi.api.mapper.DtoMappers;
import co.com.nequi.api.dto.BranchCreateRequestDto;
import co.com.nequi.api.dto.BranchResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.usecase.branch.BranchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchUseCase useCase;

    public Mono<ServerResponse> create(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return req.bodyToMono(BranchCreateRequestDto.class)
                .flatMap(b -> useCase.createBranch(fid,
                        co.com.nequi.model.branch.Branch.builder().name(b.getName()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> getByFranchise(ServerRequest req) {
        String fid = req.pathVariable("fid");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(useCase.getByFranchiseId(fid).map(DtoMappers::toRes), BranchResponseDto.class);
    }

    public Mono<ServerResponse> getById(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return useCase.getById(bid)
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> updateName(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> useCase.updateName(bid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return useCase.delete(bid).then(ServerResponse.noContent().build());
    }

}
