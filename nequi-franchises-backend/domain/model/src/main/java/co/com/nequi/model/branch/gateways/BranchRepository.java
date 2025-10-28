package co.com.nequi.model.branch.gateways;

import co.com.nequi.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {
    Mono<Branch> save(Branch branch);
    Mono<Branch> findById(String franchiseId, String branchId);
    Flux<Branch> findByFranchiseId(String franchiseId);
    Mono<Branch> update(Branch branch);
    Mono<Void> deleteById(String franchiseId, String branchId);
}
