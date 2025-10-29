package co.com.nequi.usecase.product;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.product.Product;
import co.com.nequi.model.product.gateways.ProductRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import co.com.nequi.usecase.util.FunctionUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.nequi.usecase.constant.ExceptionMessage.BRANCH_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_NOT_FOUND;
import static co.com.nequi.usecase.constant.ExceptionMessage.PRODUCT_STOCK_INVALID;

@RequiredArgsConstructor

public class ProductUseCase {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public Mono<Product> createProduct(String branchId, Product product) {
        validateProductData(product.getName(), product.getStock());

        return validateBranchExists(branchId)
                .map(branch -> product.toBuilder()
                        .id(FunctionUtils.newId())
                        .franchiseId(branch.getFranchiseId())
                        .branchId(branchId)
                        .createdAt(FunctionUtils.now())
                        .updatedAt(FunctionUtils.now())
                        .build())
                .flatMap(productRepository::save);
    }

    public Mono<Product> getById(String productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(PRODUCT_NOT_FOUND)));
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
                .map(product -> product.toBuilder()
                        .name(newName)
                        .updatedAt(FunctionUtils.now())
                        .build())
                .flatMap(productRepository::update);
    }

    public Mono<Product> updateStock(String productId, Integer newStock) {
        validateStock(newStock);

        return getById(productId)
                .map(product -> product.toBuilder()
                        .stock(newStock)
                        .updatedAt(FunctionUtils.now())
                        .build())
                .flatMap(productRepository::update);
    }

    public Mono<Void> delete(String productId) {
        return getById(productId)
                .flatMap(product -> productRepository.deleteById(productId));
    }

    public Mono<Product> getMaxStockByBranch(String branchId) {
        return productRepository.findByBranchId(branchId)
                .reduce((p1, p2) -> p1.getStock() > p2.getStock() ? p1 : p2)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(PRODUCT_NOT_FOUND)));
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

    private Mono<Branch> validateBranchExists(String branchId) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(BRANCH_NOT_FOUND)));
    }

}
