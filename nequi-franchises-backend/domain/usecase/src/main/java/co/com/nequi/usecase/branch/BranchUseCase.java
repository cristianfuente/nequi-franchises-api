package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.model.pagination.PageResult;
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
        return ReactorChecks.validateNotEmptyValue(draft.getName(), BRANCH_NAME_REQUIRED)
                .then(ReactorChecks.notFoundIfEmpty(franchiseRepository.findById(franchiseId), FRANCHISE_NOT_FOUND))
                .then(branchRepository.save(
                        draft.toBuilder()
                                .id(FunctionUtils.newId())
                                .franchiseId(franchiseId)
                                .createdAt(FunctionUtils.now())
                                .updatedAt(FunctionUtils.now())
                                .build()));
    }

    public Mono<Branch> getById(String branchId) {
        return ReactorChecks.notFoundIfEmpty(branchRepository.findById(branchId), BRANCH_NOT_FOUND);
    }

    public Mono<PageResult<Branch>> getByFranchiseId(String franchiseId, Integer limit, String cursor) {
        return branchRepository.findByFranchiseId(franchiseId, resolveLimit(limit), cursor);
    }

    public Flux<Branch> streamByFranchiseId(String franchiseId) {
        return branchRepository.streamByFranchiseId(franchiseId);
    }

    public Mono<Branch> updateName(String branchId, String newName) {
        return getById(branchId)
                .flatMap(branch ->
                        branchRepository.update(
                                branch.toBuilder()
                                        .name(newName)
                                        .updatedAt(FunctionUtils.now())
                                        .build()));
    }

    public Mono<Void> delete(String branchId) {
        return getById(branchId).then(branchRepository.deleteById(branchId));
    }

    private int resolveLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return 20;
        }
        return Math.min(requested, 100);
    }
}
