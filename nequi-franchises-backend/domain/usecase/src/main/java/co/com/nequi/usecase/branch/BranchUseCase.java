package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.util.FunctionUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public Mono<Branch> createBranch(String franchiseId, Branch branch) {
        FunctionUtils.validateNotEmptyValue(branch.getName(),
                ExceptionMessage.BRANCH_NAME_REQUIRED);

        return validateFranchiseExists(franchiseId)
                .map(franchise -> branch.toBuilder()
                        .id(UUID.randomUUID().toString())
                        .franchiseId(franchiseId)
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .build())
                .flatMap(branchRepository::save);
    }

    public Mono<Branch> getById(String branchId) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(ExceptionMessage.BRANCH_NOT_FOUND)));
    }

    public Flux<Branch> getByFranchiseId(String franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId);
    }

    public Flux<Branch> getAll() {
        return branchRepository.findAll();
    }

    public Mono<Branch> updateName(String branchId, String newName) {
        FunctionUtils.validateNotEmptyValue(newName,
                ExceptionMessage.BRANCH_NAME_REQUIRED);

        return getById(branchId)
                .map(branch -> branch.toBuilder()
                        .name(newName)
                        .updatedAt(System.currentTimeMillis())
                        .build())
                .flatMap(branchRepository::update);
    }

    public Mono<Void> delete(String branchId) {
        return getById(branchId)
                .flatMap(branch -> branchRepository.deleteById(branchId));
    }

    private Mono<Franchise> validateFranchiseExists(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(ExceptionMessage.FRANCHISE_NOT_FOUND)));
    }

}
