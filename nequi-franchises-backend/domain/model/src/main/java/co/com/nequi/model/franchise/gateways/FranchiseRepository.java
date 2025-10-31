package co.com.nequi.model.franchise.gateways;

import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.pagination.PageResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Mono<PageResult<Franchise>> findAll(int limit, String exclusiveStartKey);

    Flux<Franchise> streamAll();

    Mono<Franchise> update(Franchise franchise);

    Mono<Void> deleteById(String id);

}
