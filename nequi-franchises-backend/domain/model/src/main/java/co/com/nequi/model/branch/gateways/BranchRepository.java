package co.com.nequi.model.branch.gateways;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.pagination.PageResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(String id);

    Mono<Branch> findByIdAndFranchiseId(String id, String franchiseId);

    Mono<PageResult<Branch>> findByFranchiseId(String franchiseId, int limit, String exclusiveStartKey);

    Flux<Branch> streamByFranchiseId(String franchiseId);

    Mono<Branch> update(Branch branch);

    Mono<Void> deleteById(String id);

}
