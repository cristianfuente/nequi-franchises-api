package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public Mono<Branch> createBranch(String franchiseId, Branch branch) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {

                    if (branch.getName() == null || branch.getName().trim().isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Branch name is required"));
                    }

                    Branch entity = branch
                            .toBuilder()
                            .id(UUID.randomUUID().toString())
                            .franchiseId(franchiseId)
                            .createdAt(System.currentTimeMillis())
                            .updatedAt(System.currentTimeMillis())
                            .build();
                    return branchRepository.save(entity);
                });
    }

    public Mono<Branch> getById(String franchiseId, String branchId) {
        return branchRepository.findById(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Branch not found with id: " + branchId)));
    }

    public Flux<Branch> getByFranchiseId(String franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId);
    }

    public Mono<Branch> updateBranch(String franchiseId, String branchId, Branch updatedBranch) {
        return branchRepository.findById(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Branch not found with id: " + branchId)))
                .flatMap(existingBranch -> {

                    Branch entity = updatedBranch.toBuilder()
                            .id(existingBranch.getId())
                            .franchiseId(existingBranch.getFranchiseId())
                            .createdAt(existingBranch.getCreatedAt())
                            .updatedAt(System.currentTimeMillis())
                            .build();

                    return branchRepository.update(entity);
                });
    }

    public Mono<Void> deleteBranch(String franchiseId, String branchId) {
        return branchRepository.findById(franchiseId, branchId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Branch not found with id: " + branchId)))
                .flatMap(branch -> branchRepository.deleteById(franchiseId, branchId));
    }

}
