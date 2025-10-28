package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;


    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private BranchUseCase branchUseCase;

    private Franchise franchise;
    private Branch branch;
    private String franchiseId;
    private String branchId;

    @BeforeEach
    void setUp() {
        franchiseId = "franchise-123";
        branchId = "branch-456";

        franchise = Franchise.builder()
                .id(franchiseId)
                .name("Test Franchise")
                .build();

        branch = Branch.builder()
                .id(branchId)
                .franchiseId(franchiseId)
                .name("Test Branch")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void createBranch_Success() {
        // Arrange
        Branch newBranch = Branch.builder()
                .name("New Branch")
                .build();

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(branchUseCase.createBranch(franchiseId, newBranch))
                .expectNextMatches(savedBranch ->
                        savedBranch.getName().equals("New Branch") &&
                                savedBranch.getFranchiseId().equals(franchiseId) &&
                                savedBranch.getId() != null &&
                                savedBranch.getCreatedAt() != null &&
                                savedBranch.getUpdatedAt() != null
                )
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void createBranch_FranchiseNotFound() {
        Branch newBranch = Branch.builder()
                .name("New Branch")
                .build();

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        StepVerifier.create(branchUseCase.createBranch(franchiseId, newBranch))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Franchise not found with id: " + franchiseId)
                )
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void createBranch_NameIsNull() {
        Branch newBranch = Branch.builder()
                .name(null)
                .build();

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));

        StepVerifier.create(branchUseCase.createBranch(franchiseId, newBranch))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch name is required")
                )
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Should throw error when branch name is empty")
    void createBranch_NameIsEmpty() {
        Branch newBranch = Branch.builder()
                .name("")
                .build();

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));

        StepVerifier.create(branchUseCase.createBranch(franchiseId, newBranch))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch name is required")
                )
                .verify();

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void createBranch_NameIsBlank() {
        // Arrange
        Branch newBranch = Branch.builder()
                .name("   ")
                .build();

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));

        // Act & Assert
        StepVerifier.create(branchUseCase.createBranch(franchiseId, newBranch))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch name is required")
                )
                .verify();

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void getById_Success() {
        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.just(branch));

        StepVerifier.create(branchUseCase.getById(franchiseId, branchId))
                .expectNext(branch)
                .verifyComplete();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
    }

    @Test
    void getById_NotFound() {
        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.empty());

        StepVerifier.create(branchUseCase.getById(franchiseId, branchId))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch not found with id: " + branchId)
                )
                .verify();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
    }

    @Test
    void getByFranchiseId_Success() {
        // Arrange
        Branch branch1 = Branch.builder()
                .id("branch-1")
                .franchiseId(franchiseId)
                .name("Branch 1")
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .franchiseId(franchiseId)
                .name("Branch 2")
                .build();

        when(branchRepository.findByFranchiseId(franchiseId))
                .thenReturn(Flux.just(branch1, branch2));

        // Act & Assert
        StepVerifier.create(branchUseCase.getByFranchiseId(franchiseId))
                .expectNext(branch1)
                .expectNext(branch2)
                .verifyComplete();

        verify(branchRepository, times(1)).findByFranchiseId(franchiseId);
    }

    @Test
    void getByFranchiseId_EmptyResult() {
        when(branchRepository.findByFranchiseId(franchiseId))
                .thenReturn(Flux.empty());

        StepVerifier.create(branchUseCase.getByFranchiseId(franchiseId))
                .verifyComplete();

        verify(branchRepository, times(1)).findByFranchiseId(franchiseId);
    }

    @Test
    void updateBranch_Success() {
        // Arrange
        Branch updatedBranch = Branch.builder()
                .name("Updated Branch Name")
                .build();

        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.just(branch));
        when(branchRepository.update(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(branchUseCase.updateBranch(franchiseId, branchId, updatedBranch))
                .expectNextMatches(result ->
                        result.getName().equals("Updated Branch Name") &&
                                result.getId().equals(branchId) &&
                                result.getFranchiseId().equals(franchiseId) &&
                                result.getCreatedAt().equals(branch.getCreatedAt()) &&
                                result.getUpdatedAt() > branch.getUpdatedAt()
                )
                .verifyComplete();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
        verify(branchRepository, times(1)).update(any(Branch.class));
    }

    @Test
    void updateBranch_NotFound() {
        // Arrange
        Branch updatedBranch = Branch.builder()
                .name("Updated Branch Name")
                .build();

        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(branchUseCase.updateBranch(franchiseId, branchId, updatedBranch))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch not found with id: " + branchId)
                )
                .verify();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
        verify(branchRepository, never()).update(any(Branch.class));
    }

    @Test
    void updateBranch_PreservesOriginalData() {
        long originalCreatedAt = branch.getCreatedAt();

        Branch updatedBranch = Branch.builder()
                .name("New Name")
                .build();

        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.just(branch));
        when(branchRepository.update(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(branchUseCase.updateBranch(franchiseId, branchId, updatedBranch))
                .expectNextMatches(result ->
                        result.getId().equals(branchId) &&
                                result.getFranchiseId().equals(franchiseId) &&
                                result.getCreatedAt().equals(originalCreatedAt)
                )
                .verifyComplete();
    }

    @Test
    void deleteBranch_Success() {
        // Arrange
        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.just(branch));
        when(branchRepository.deleteById(franchiseId, branchId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(branchUseCase.deleteBranch(franchiseId, branchId))
                .verifyComplete();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
        verify(branchRepository, times(1)).deleteById(franchiseId, branchId);
    }

    @Test
    void deleteBranch_NotFound() {
        // Arrange
        when(branchRepository.findById(franchiseId, branchId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(branchUseCase.deleteBranch(franchiseId, branchId))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Branch not found with id: " + branchId)
                )
                .verify();

        verify(branchRepository, times(1)).findById(franchiseId, branchId);
        verify(branchRepository, never()).deleteById(anyString(), anyString());
    }

}