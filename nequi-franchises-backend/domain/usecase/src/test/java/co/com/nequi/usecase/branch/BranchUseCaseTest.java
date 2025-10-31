package co.com.nequi.usecase.branch;

import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.usecase.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchUseCaseTest {

    @Mock
    BranchRepository branchRepository;
    @Mock
    FranchiseRepository franchiseRepository;

    BranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BranchUseCase(branchRepository, franchiseRepository);
    }

    @Test
    void createBranch_ok() {
        String fid = "F1";
        Branch draft = Branch.builder().name("Centro").build();

        when(franchiseRepository.findById(fid)).thenReturn(Mono.just(Franchise.builder().id(fid).name("Fran").build()));
        when(branchRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createBranch(fid, draft))
                .assertNext(b -> {
                    assert b.getId() != null;
                    assert fid.equals(b.getFranchiseId());
                    assert b.getCreatedAt() != null && b.getUpdatedAt() != null;
                })
                .verifyComplete();

        verify(franchiseRepository).findById(fid);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void createBranch_name_blank_error() {
        String fid = "F1";
        Branch draft = Branch.builder().name(" ").build();

        when(franchiseRepository.findById(fid)).thenReturn(Mono.just(Franchise.builder().id(fid).name("Fran").build()));
        when(branchRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createBranch(fid, draft))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void getByFranchiseId_ok() {
        when(branchRepository.findByFranchiseId("F1")).thenReturn(Flux.just(
                Branch.builder().id("B1").franchiseId("F1").name("A").build(),
                Branch.builder().id("B2").franchiseId("F1").name("B").build()
        ));

        StepVerifier.create(useCase.getByFranchiseId("F1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void updateName_ok() {
        Branch existing = Branch.builder().id("B1").franchiseId("F1").name("Old").updatedAt(1L).build();
        when(branchRepository.findById("B1")).thenReturn(Mono.just(existing));
        when(branchRepository.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.updateName("B1", "New"))
                .assertNext(b -> {
                    assert "New".equals(b.getName());
                    assert b.getUpdatedAt() != null;
                })
                .verifyComplete();

        verify(branchRepository).findById("B1");
        verify(branchRepository).update(any(Branch.class));
    }

    @Test
    void delete_ok() {
        when(branchRepository.findById("B1")).thenReturn(Mono.just(Branch.builder().id("B1").build()));
        when(branchRepository.deleteById("B1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("B1")).verifyComplete();

        verify(branchRepository).findById("B1");
        verify(branchRepository).deleteById("B1");
    }

}