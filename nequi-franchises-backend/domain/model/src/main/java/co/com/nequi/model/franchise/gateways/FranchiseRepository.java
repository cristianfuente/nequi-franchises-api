package co.com.nequi.model.franchise.gateways;

import co.com.nequi.model.franchise.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Flux<Franchise> findAll();

    Mono<Franchise> update(Franchise franchise);

    Mono<Void> deleteById(String id);

}
