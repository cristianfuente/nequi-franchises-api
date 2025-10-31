package co.com.nequi.api.handler;

import co.com.nequi.api.DtoMappers;
import co.com.nequi.api.dto.ChangeStockRequestDto;
import co.com.nequi.api.dto.ProductCreateRequestDto;
import co.com.nequi.api.dto.ProductResponseDto;
import co.com.nequi.api.dto.RenameRequestDto;
import co.com.nequi.api.dto.TopProductItemResponseDto;
import co.com.nequi.usecase.branch.BranchUseCase;
import co.com.nequi.usecase.product.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.api.DtoMappers.toSummary;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;
    private final BranchUseCase branchUseCase; // para componer top por franquicia

    public Mono<ServerResponse> create(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return req.bodyToMono(ProductCreateRequestDto.class)
                .flatMap(b -> productUseCase.createProduct(bid,
                        co.com.nequi.model.product.Product.builder()
                                .name(b.getName()).stock(b.getStock()).build()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.created(req.uri()).contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> listByBranch(ServerRequest req) {
        String bid = req.pathVariable("bid");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productUseCase.getByBranchId(bid).map(DtoMappers::toRes), ProductResponseDto.class);
    }

    public Mono<ServerResponse> updateName(ServerRequest req) {
        String pid = req.pathVariable("pid");
        return req.bodyToMono(RenameRequestDto.class)
                .flatMap(b -> productUseCase.updateName(pid, b.getName()))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> changeStock(ServerRequest req) {
        String pid = req.pathVariable("pid");
        String idem = req.headers().firstHeader("Idempotency-Key");
        return req.bodyToMono(ChangeStockRequestDto.class)
                .flatMap(b -> productUseCase.changeStock(pid, b.getDelta(), idem))
                .map(DtoMappers::toRes)
                .flatMap(res -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(res));
    }

    public Mono<ServerResponse> deleteByBranch(ServerRequest req) {
        String bid = req.pathVariable("bid");
        String pid = req.pathVariable("pid");
        return productUseCase.deleteByBranch(bid, pid)
                .then(ServerResponse.noContent().build());
    }

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
