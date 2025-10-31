package co.com.nequi.usecase.franchise;

import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.model.pagination.PageResult;
import co.com.nequi.usecase.util.FunctionUtils;
import co.com.nequi.usecase.util.ReactorChecks;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NOT_FOUND;

@RequiredArgsConstructor
public class FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> createFranchise(Franchise draft) {
        return ReactorChecks.validateNotEmptyValue(draft.getName(), FRANCHISE_NAME_REQUIRED)
                .then(franchiseRepository.save(draft.toBuilder()
                        .id(FunctionUtils.newId())
                        .createdAt(FunctionUtils.now())
                        .updatedAt(FunctionUtils.now())
                        .build()));
    }

    public Mono<Franchise> getById(String franchiseId) {
        return ReactorChecks.notFoundIfEmpty(franchiseRepository.findById(franchiseId), FRANCHISE_NOT_FOUND);
    }

    public Mono<PageResult<Franchise>> getAll(Integer limit, String cursor) {
        return franchiseRepository.findAll(resolveLimit(limit), cursor);
    }

    public Flux<Franchise> streamAll() {
        return franchiseRepository.streamAll();
    }

    public Mono<Franchise> updateName(String franchiseId, String newName) {
        return getById(franchiseId)
                .flatMap(franchise -> franchiseRepository.update(franchise.toBuilder()
                        .name(newName)
                        .updatedAt(FunctionUtils.now())
                        .build()));
    }

    public Mono<Void> delete(String franchiseId) {
        return getById(franchiseId)
                .then(franchiseRepository.deleteById(franchiseId));
    }

    private int resolveLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return 20;
        }
        return Math.min(requested, 100);
    }
}
