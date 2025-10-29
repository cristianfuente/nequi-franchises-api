package co.com.nequi.usecase.franchise;

import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import co.com.nequi.usecase.util.FunctionUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.SAME_NAME;

@RequiredArgsConstructor
public class FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> createFranchise(Franchise franchise) {
        FunctionUtils.validateNotEmptyValue(franchise.getName(), FRANCHISE_NAME_REQUIRED);

        Franchise newFranchise = franchise.toBuilder()
                .id(FunctionUtils.newId())
                .createdAt(FunctionUtils.now())
                .updatedAt(FunctionUtils.now())
                .build();

        return franchiseRepository.save(newFranchise);
    }

    public Mono<Franchise> updateName(String franchiseId, String newName) {
        FunctionUtils.validateNotEmptyValue(newName, FRANCHISE_NAME_REQUIRED);

        return getById(franchiseId)
                .map(franchise -> {
                    if (newName.equals(franchise.getName())) {
                        throw new ValidationException(SAME_NAME);
                    }
                    return franchise.toBuilder()
                            .name(newName)
                            .updatedAt(System.currentTimeMillis())
                            .build();
                })
                .flatMap(franchiseRepository::update);
    }

    public Mono<Void> delete(String franchiseId) {
        return getById(franchiseId)
                .flatMap(franchise -> franchiseRepository.deleteById(franchiseId));
    }

    public Mono<Franchise> getById(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(FRANCHISE_NOT_FOUND)));
    }

    public Flux<Franchise> getAll() {
        return franchiseRepository.findAll();
    }

}
