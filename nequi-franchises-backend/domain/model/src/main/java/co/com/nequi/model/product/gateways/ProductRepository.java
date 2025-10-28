package co.com.nequi.model.product.gateways;

import co.com.nequi.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ProductRepository {
    Mono<Product> save(Product product);

    Mono<Product> findByName(String branchId, String productName);

    Flux<Product> findByBranchId(String branchId);

    Flux<Product> findByFranchiseId(String franchiseId);

    Mono<Product> updateStock(String branchId, String productName, BigDecimal newStock);

    Mono<Void> deleteByName(String branchId, String productName);
}
