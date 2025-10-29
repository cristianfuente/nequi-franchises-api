package co.com.nequi.model.product.gateways;

import co.com.nequi.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Mono<Product> findByIdAndBranchId(String id, String branchId);

    Flux<Product> findByBranchId(String branchId);

    Flux<Product> findByFranchiseId(String franchiseId);

    Flux<Product> findAll();

    Mono<Product> update(Product product);

    Mono<Void> deleteById(String id);

}
