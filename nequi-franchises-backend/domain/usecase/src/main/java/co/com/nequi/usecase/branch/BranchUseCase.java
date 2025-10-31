package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.util.FunctionUtils;
import co.com.nequi.usecase.util.ReactorChecks;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.BRANCH_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.BRANCH_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NOT_FOUND;

@RequiredArgsConstructor
public class BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public Mono<Branch> createBranch(String franchiseId, Branch draft) {
        FunctionUtils.validateNotEmptyValue(draft.getName(), BRANCH_NAME_REQUIRED);

        return ReactorChecks.notFoundIfEmpty(franchiseRepository.findById(franchiseId), FRANCHISE_NOT_FOUND)
                .flatMap(f -> Mono.defer(() -> {
                    long now = FunctionUtils.now();
                    var branch = draft.toBuilder()
                            .id(FunctionUtils.newId())
                            .franchiseId(franchiseId)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return branchRepository.save(branch);
                }));
    }

    public Mono<Branch> getById(String branchId) {
        return ReactorChecks.notFoundIfEmpty(branchRepository.findById(branchId), BRANCH_NOT_FOUND);
    }

    public Flux<Branch> getByFranchiseId(String franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId);
    }

    public Flux<Branch> getAll() {
        return branchRepository.findAll();
    }

    public Mono<Branch> updateName(String branchId, String newName) {
        FunctionUtils.validateNotEmptyValue(newName, BRANCH_NAME_REQUIRED);

        return getById(branchId)
                .flatMap(b -> {
                    var updated = b.toBuilder()
                            .name(newName)
                            .updatedAt(FunctionUtils.now())
                            .build();
                    return branchRepository.update(updated);
                });
    }

    public Mono<Void> delete(String branchId) {
        return getById(branchId).then(branchRepository.deleteById(branchId));
    }

}
