package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.BranchEntity;
import co.com.nequi.dynamodb.helper.TemplateAdapterOperations;
import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Objects;

@Repository
public class DynamoBranchAdapter extends TemplateAdapterOperations<Branch, String, BranchEntity>
        implements BranchRepository {

    private final DynamoDbAsyncTable<BranchEntity> branchTable;
    private final ObjectMapper mapper;

    public DynamoBranchAdapter(DynamoDbEnhancedAsyncClient enhanced,
                               ObjectMapper mapper,
                               @Value("${app.dynamo.branch-table}") String tableName) {
        super(enhanced, mapper, be -> mapper.map(be, Branch.class), tableName, "byFranchise");
        this.mapper = mapper;
        this.branchTable = enhanced.table(tableName, TableSchema.fromBean(BranchEntity.class));
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        return super.save(branch);
    }

    @Override
    public Mono<Branch> findById(String id) {
        return super.getById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.BRANCH_NOT_FOUND)));
    }

    @Override
    public Mono<Branch> findByIdAndFranchiseId(String id, String franchiseId) {
        return findById(id)
                .filter(b -> Objects.equals(b.getFranchiseId(), franchiseId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.BRANCH_NOT_FOUND)));
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        var index = branchTable.index("byFranchise");
        var req = software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(franchiseId)))
                .build();
        SdkPublisher<Page<BranchEntity>> pub = index.query(req);
        return Flux.from(pub).flatMapIterable(Page::items)
                .map(be -> mapper.map(be, Branch.class));
    }

    @Override
    public Flux<Branch> findAll() {
        PagePublisher<BranchEntity> pub = branchTable.scan();
        return Flux.from(pub).flatMapIterable(Page::items)
                .map(be -> mapper.map(be, Branch.class));
    }

    @Override
    public Mono<Branch> update(Branch branch) {
        return Mono.fromFuture(branchTable.putItem(mapper.map(branch, BranchEntity.class)))
                .thenReturn(branch);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(b -> Mono.fromFuture(branchTable.deleteItem(Key.builder().partitionValue(id).build())))
                .then();
    }

}
