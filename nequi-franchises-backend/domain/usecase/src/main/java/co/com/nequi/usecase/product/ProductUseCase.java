package co.com.nequi.usecase.product;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.product.Product;
import co.com.nequi.model.product.gateways.ProductRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import co.com.nequi.usecase.util.FunctionUtils;
import co.com.nequi.usecase.util.ReactorChecks;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.BRANCH_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.IDEMPOTENCY_KEY_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.NO_PRODUCTS_IN_BRANCH;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_STOCK_INVALID;

@RequiredArgsConstructor

public class ProductUseCase {


    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public Mono<Product> createProduct(String branchId, Product draft) {
        validateProductData(draft.getName(), draft.getStock());

        return ReactorChecks.notFoundIfEmpty(branchRepository.findById(branchId), BRANCH_NOT_FOUND)
                .flatMap(br -> Mono.defer(() -> {
                    long now = FunctionUtils.now();
                    var p = draft.toBuilder()
                            .id(FunctionUtils.newId())
                            .franchiseId(br.getFranchiseId())
                            .branchId(branchId)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return productRepository.save(p);
                }));
    }

    public Mono<Product> getById(String productId) {
        return ReactorChecks.notFoundIfEmpty(productRepository.findById(productId), PRODUCT_NOT_FOUND);
    }

    public Flux<Product> getByBranchId(String branchId) {
        return productRepository.findByBranchId(branchId);
    }

    public Flux<Product> getByFranchiseId(String franchiseId) {
        return productRepository.findByFranchiseId(franchiseId);
    }

    public Flux<Product> getAll() {
        return productRepository.findAll();
    }

    public Mono<Product> updateName(String productId, String newName) {
        FunctionUtils.validateNotEmptyValue(newName, PRODUCT_NAME_REQUIRED);

        return getById(productId)
                .flatMap(p -> productRepository.update(
                        p.toBuilder()
                                .name(newName)
                                .updatedAt(FunctionUtils.now())
                                .build()
                ));
    }

    public Mono<Product> getMaxStockByBranch(String branchId) {
        return productRepository.findTopByBranchId(branchId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(NO_PRODUCTS_IN_BRANCH)));
    }

    public Mono<Product> changeStock(String productId, int delta, String idempotencyKey) {
        if (delta == 0) return getById(productId);
        FunctionUtils.validateNotEmptyValue(idempotencyKey, IDEMPOTENCY_KEY_REQUIRED);

        return getById(productId)
                .flatMap(p -> productRepository.changeStockAtomic(p.getId(), delta, idempotencyKey));
    }

    public Mono<Void> deleteByBranch(String branchId, String productId) {
        return ReactorChecks.notFoundIfEmpty(branchRepository.findById(branchId), BRANCH_NOT_FOUND)
                .then(productRepository.deleteByBranchAndId(branchId, productId));
    }

    public Mono<Void> delete(String productId) {
        return getById(productId).then(productRepository.deleteById(productId));
    }

    private void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new ValidationException(PRODUCT_STOCK_INVALID);
        }
    }

    private void validateProductData(String name, Integer stock) {
        FunctionUtils.validateNotEmptyValue(name, PRODUCT_NAME_REQUIRED);
        validateStock(stock);
    }

}
