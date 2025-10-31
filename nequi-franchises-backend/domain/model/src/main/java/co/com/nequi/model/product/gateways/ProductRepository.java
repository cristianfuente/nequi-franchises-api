package co.com.nequi.model.product.gateways;

import co.com.nequi.model.pagination.PageResult;
import co.com.nequi.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Mono<Product> findByIdAndBranchId(String id, String branchId);

    Mono<PageResult<Product>> findByBranchId(String branchId, int limit, String exclusiveStartKey);

    Mono<PageResult<Product>> findByFranchiseId(String franchiseId, int limit, String exclusiveStartKey);

    Mono<PageResult<Product>> searchByName(String branchId, String prefix, int limit, String exclusiveStartKey);

    Flux<Product> streamByBranch(String branchId);

    Mono<Product> changeStockAtomic(String productId, int delta, String idempotencyKey);

    Mono<Product> update(Product product);

    Mono<Void> deleteById(String id);

    Mono<Void> deleteByBranchAndId(String branchId, String productId);

    Mono<Product> findTopByBranchId(String branchId);

}
