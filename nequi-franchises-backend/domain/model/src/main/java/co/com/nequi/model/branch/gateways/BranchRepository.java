package co.com.nequi.model.branch.gateways;

import co.com.nequi.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(String id);

    Mono<Branch> findByIdAndFranchiseId(String id, String franchiseId);

    Flux<Branch> findByFranchiseId(String franchiseId);

    Flux<Branch> findAll();

    Mono<Branch> update(Branch branch);

    Mono<Void> deleteById(String id);

}
