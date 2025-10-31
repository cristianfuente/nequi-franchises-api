package co.com.nequi.usecase.product;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.product.Product;
import co.com.nequi.model.product.gateways.ProductRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductUseCaseTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    BranchRepository branchRepository;

    ProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductUseCase(productRepository, branchRepository);
    }

    @Test
    void createProduct_ok() {
        String bid = "B1";
        Branch branch = Branch.builder().id(bid).franchiseId("F1").name("Centro").build();
        Product draft = Product.builder().name("Leche").stock(5).build();

        when(branchRepository.findById(bid)).thenReturn(Mono.just(branch));
        when(productRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createProduct(bid, draft))
                .assertNext(p -> {
                    assert p.getId() != null;
                    assert "F1".equals(p.getFranchiseId());
                    assert bid.equals(p.getBranchId());
                })
                .verifyComplete();

        verify(branchRepository).findById(bid);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_invalidStock_error() {
        Product draft = Product.builder().name("Leche").stock(-1).build();
        Branch branch = Branch.builder().id("B1").franchiseId("F1").name("Centro").build();

        when(branchRepository.findById(any())).thenReturn(Mono.just(branch));
        when(productRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createProduct("B1", draft))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void getMaxStockByBranch_ok() {
        String bid = "B1";
        when(productRepository.findTopByBranchId(bid)).thenReturn(Mono.just(Product.builder().id("P1").stock(10).build()));

        StepVerifier.create(useCase.getMaxStockByBranch(bid))
                .assertNext(p -> Assertions.assertEquals(p.getStock(), 10))
                .verifyComplete();

        verify(productRepository).findTopByBranchId(bid);
    }

    @Test
    void getMaxStockByBranch_empty_error() {
        when(productRepository.findTopByBranchId("B404")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getMaxStockByBranch("B404"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void changeStock_deltaZero_returnsExisting() {
        when(productRepository.findById("P1")).thenReturn(Mono.just(Product.builder().id("P1").stock(7).build()));

        StepVerifier.create(useCase.changeStock("P1", 0, "K"))
                .assertNext(p -> Assertions.assertEquals(p.getStock(), 7))
                .verifyComplete();

        verify(productRepository).findById("P1");
        verify(productRepository, never()).changeStockAtomic(anyString(), anyInt(), anyString());
    }

    @Test
    void changeStock_withDelta_andIdempotency_ok() {
        when(productRepository.findById("P1")).thenReturn(Mono.just(Product.builder().id("P1").stock(7).build()));
        when(productRepository.changeStockAtomic("P1", 3, "K")).thenReturn(Mono.just(Product.builder().id("P1").stock(10).build()));

        StepVerifier.create(useCase.changeStock("P1", 3, "K"))
                .assertNext(p -> Assertions.assertEquals(p.getStock(), 10))
                .verifyComplete();

        verify(productRepository).findById("P1");
        verify(productRepository).changeStockAtomic("P1", 3, "K");
    }

    @Test
    void changeStock_missingIdempotency_error() {
        when(productRepository.findById("P1")).thenReturn(Mono.just(Product.builder().id("P1").stock(7).build()));

        StepVerifier.create(useCase.changeStock("P1", 1, " "))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void deleteByBranch_ok() {
        when(branchRepository.findById("B1")).thenReturn(Mono.just(Branch.builder().id("B1").build()));
        when(productRepository.deleteByBranchAndId("B1", "P1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteByBranch("B1", "P1")).verifyComplete();

        verify(branchRepository).findById("B1");
        verify(productRepository).deleteByBranchAndId("B1", "P1");
    }

    @Test
    void delete_ok() {
        when(productRepository.findById("P1")).thenReturn(Mono.just(Product.builder().id("P1").build()));
        when(productRepository.deleteById("P1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("P1")).verifyComplete();

        verify(productRepository).findById("P1");
        verify(productRepository).deleteById("P1");
    }

}