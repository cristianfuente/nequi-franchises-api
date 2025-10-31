package co.com.nequi.usecase.franchise;

import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NAME_REQUIRED;
import static co.com.nequi.usecase.constant.ExceptionMessage.FRANCHISE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseUseCase franchiseUseCase;

    private Franchise franchise;
    private String franchiseId;

    @BeforeEach
    void setUp() {
        franchiseId = "franchise-123";
        franchise = Franchise.builder()
                .id(franchiseId)
                .name("Test Franchise")
                .createdAt(1234567890L)
                .updatedAt(1234567890L)
                .build();
    }

    @Test
    void createFranchise_Success() {
        Franchise newFranchise = Franchise.builder().name("New Franchise").build();
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(franchiseUseCase.createFranchise(newFranchise))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo("New Franchise");
                    assertThat(saved.getId()).isNotNull();
                    assertThat(saved.getCreatedAt()).isNotNull();
                    assertThat(saved.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }

    @Test
    void createFranchise_WithNullName_ThrowsValidationException() {
        Franchise newFranchise = Franchise.builder().name(null).build();

        StepVerifier.create(franchiseUseCase.createFranchise(newFranchise))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals(FRANCHISE_NAME_REQUIRED)
                )
                .verify();

        verify(franchiseRepository, never()).save(any(Franchise.class));
    }

    @Test
    void createFranchise_WithEmptyName_ThrowsValidationException() {
        Franchise newFranchise = Franchise.builder().name("").build();

        StepVerifier.create(franchiseUseCase.createFranchise(newFranchise))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals(FRANCHISE_NAME_REQUIRED)
                )
                .verify();

        verify(franchiseRepository, never()).save(any(Franchise.class));
    }

    @Test
    void createFranchise_WithBlankName_ThrowsValidationException() {
        Franchise newFranchise = Franchise.builder().name("   ").build();

        StepVerifier.create(franchiseUseCase.createFranchise(newFranchise))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals(FRANCHISE_NAME_REQUIRED)
                )
                .verify();

        verify(franchiseRepository, never()).save(any(Franchise.class));
    }

    @Test
    void getById_Success() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseUseCase.getById(franchiseId))
                .expectNext(franchise)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.getById(franchiseId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().equals(FRANCHISE_NOT_FOUND)
                )
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
    }

    @Test
    void getAll_Success() {
        Franchise franchise1 = Franchise.builder().id("1").name("Franchise 1").build();
        Franchise franchise2 = Franchise.builder().id("2").name("Franchise 2").build();

        when(franchiseRepository.findAll())
                .thenReturn(Flux.just(franchise1, franchise2));

        StepVerifier.create(franchiseUseCase.getAll())
                .expectNext(franchise1)
                .expectNext(franchise2)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll();
    }

    @Test
    void getAll_EmptyResult() {
        when(franchiseRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(franchiseUseCase.getAll())
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll();
    }

    @Test
    void updateName_Success() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(franchiseRepository.update(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(franchiseUseCase.updateName(franchiseId, "New Name"))
                .assertNext(updated -> {
                    assertThat(updated.getName()).isEqualTo("New Name");
                    assertThat(updated.getId()).isEqualTo(franchiseId);
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, times(1)).update(any(Franchise.class));
    }

    @Test
    void updateName_WithNullName_ThrowsValidationException() {
        StepVerifier.create(franchiseUseCase.updateName(franchiseId, null))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals(FRANCHISE_NAME_REQUIRED)
                )
                .verify();

        verify(franchiseRepository, never()).findById(anyString());
        verify(franchiseRepository, never()).update(any(Franchise.class));
    }

    @Test
    void updateName_WithEmptyName_ThrowsValidationException() {
        StepVerifier.create(franchiseUseCase.updateName(franchiseId, ""))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals(FRANCHISE_NAME_REQUIRED)
                )
                .verify();

        verify(franchiseRepository, never()).findById(anyString());
        verify(franchiseRepository, never()).update(any(Franchise.class));
    }

    @Test
    void updateName_FranchiseNotFound_ThrowsResourceNotFoundException() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.updateName(franchiseId, "New Name"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().equals(FRANCHISE_NOT_FOUND)
                )
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, never()).update(any(Franchise.class));
    }

    @Test
    void delete_Success() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(franchiseRepository.deleteById(franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.delete(franchiseId))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, times(1)).deleteById(franchiseId);
    }

    @Test
    void delete_FranchiseNotFound_ThrowsResourceNotFoundException() {
        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.delete(franchiseId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().equals(FRANCHISE_NOT_FOUND)
                )
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(franchiseRepository, never()).deleteById(anyString());
    }

}