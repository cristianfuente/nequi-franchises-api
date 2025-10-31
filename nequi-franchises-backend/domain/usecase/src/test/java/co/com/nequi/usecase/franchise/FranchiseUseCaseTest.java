package co.com.nequi.usecase.franchise;

import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock
    FranchiseRepository franchiseRepository;
    FranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FranchiseUseCase(franchiseRepository);
    }

    @Test
    void createFranchise_ok() {
        Franchise draft = Franchise.builder().name("Fran").build();
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createFranchise(draft))
                .assertNext(f -> {
                    assert f.getId() != null;
                    assert f.getCreatedAt() != null && f.getUpdatedAt() != null;
                })
                .verifyComplete();

        verify(franchiseRepository).save(any(Franchise.class));
    }

    @Test
    void createFranchise_blankName_error() {
        Franchise draft = Franchise.builder().name("  ").build();

        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createFranchise(draft))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void getById_notFound_error() {
        when(franchiseRepository.findById("F404")).thenReturn(Mono.empty());
        StepVerifier.create(useCase.getById("F404"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void updateName_ok() {
        when(franchiseRepository.findById("F1")).thenReturn(Mono.just(Franchise.builder().id("F1").name("Old").build()));
        when(franchiseRepository.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateName("F1", "New"))
                .assertNext(f -> Assertions.assertEquals(f.getName(), "New"))
                .verifyComplete();

        verify(franchiseRepository).findById("F1");
        verify(franchiseRepository).update(any(Franchise.class));
    }

    @Test
    void delete_ok() {
        when(franchiseRepository.findById("F1")).thenReturn(Mono.just(Franchise.builder().id("F1").build()));
        when(franchiseRepository.deleteById("F1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("F1")).verifyComplete();

        verify(franchiseRepository).findById("F1");
        verify(franchiseRepository).deleteById("F1");
    }

}