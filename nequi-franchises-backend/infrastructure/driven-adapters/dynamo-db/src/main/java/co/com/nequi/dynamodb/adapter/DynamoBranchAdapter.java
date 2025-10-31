package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.BranchEntity;
import co.com.nequi.dynamodb.helper.DynamoPaginationCodec;
import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.branch.gateways.BranchRepository;
import co.com.nequi.model.pagination.PageResult;
import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ResourceNotFoundException;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class DynamoBranchAdapter implements BranchRepository {

    private static final String ENTITY_TYPE = "BRANCH";
    private static final int STREAM_PAGE_SIZE = 100;

    private final DynamoDbAsyncTable<BranchEntity> branchTable;
    private final ObjectMapper mapper;

    public DynamoBranchAdapter(DynamoDbEnhancedAsyncClient enhanced,
                               ObjectMapper mapper,
                               @Value("${app.dynamo.core-table}") String tableName) {
        this.mapper = mapper;
        this.branchTable = enhanced.table(tableName, TableSchema.fromBean(BranchEntity.class));
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        return Mono.defer(() -> Mono.fromFuture(branchTable.putItem(mapToEntity(branch)))
                .thenReturn(branch));
    }

    @Override
    public Mono<Branch> findById(String id) {
        return Mono.defer(() -> Mono.fromFuture(branchTable.getItem(r -> r.key(Key.builder()
                                .partitionValue(id)
                                .build())))
                        .flatMap(entity -> entity == null || !ENTITY_TYPE.equals(entity.getEntityType())
                                ? Mono.empty()
                                : Mono.just(mapToDomain(entity))))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.BRANCH_NOT_FOUND)));
    }

    @Override
    public Mono<Branch> findByIdAndFranchiseId(String id, String franchiseId) {
        return findById(id)
                .filter(branch -> Objects.equals(branch.getFranchiseId(), franchiseId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.BRANCH_NOT_FOUND)));
    }

    @Override
    public Mono<PageResult<Branch>> findByFranchiseId(String franchiseId, int limit, String exclusiveStartKey) {
        final int pageSize = Math.max(1, limit);
        return Mono.defer(() -> {
            QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(franchiseId)))
                    .limit(pageSize)
                    .scanIndexForward(true)
                    .addAttributeToProject("id")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("name")
                    .addAttributeToProject("status")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version");

            Map<String, AttributeValue> startKey = DynamoPaginationCodec.decode(exclusiveStartKey);
            if (startKey != null && !startKey.isEmpty()) {
                builder.exclusiveStartKey(startKey);
            }

            return Mono.from(branchTable.index("byFranchise").query(builder.build()))
                    .map(page -> {
                        List<Branch> items = page.items().stream()
                                .filter(entity -> ENTITY_TYPE.equals(entity.getEntityType()))
                                .map(this::mapToDomain)
                                .toList();
                        String nextCursor = DynamoPaginationCodec.encode(page.lastEvaluatedKey());
                        return PageResult.of(items, nextCursor);
                    });
        });
    }

    @Override
    public Flux<Branch> streamByFranchiseId(String franchiseId) {
        return Flux.defer(() -> fetchBranchPages(franchiseId, null));
    }

    private Flux<Branch> fetchBranchPages(String franchiseId, String cursor) {
        return findByFranchiseId(franchiseId, STREAM_PAGE_SIZE, cursor)
                .flatMapMany(page -> {
                    Flux<Branch> items = Flux.fromIterable(page.getItems());
                    if (page.getLastEvaluatedKey() == null) {
                        return items;
                    }
                    return items.concatWith(fetchBranchPages(franchiseId, page.getLastEvaluatedKey()));
                });
    }

    @Override
    public Mono<Branch> update(Branch branch) {
        return Mono.defer(() -> Mono.fromFuture(branchTable.putItem(mapToEntity(branch)))
                .thenReturn(branch));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(b -> Mono.fromFuture(branchTable.deleteItem(Key.builder().partitionValue(id).build())))
                .then();
    }

    private BranchEntity mapToEntity(Branch branch) {
        BranchEntity entity = new BranchEntity();
        entity.setId(branch.getId());
        entity.setFranchiseId(branch.getFranchiseId());
        entity.setName(branch.getName());
        entity.setCreatedAt(branch.getCreatedAt());
        entity.setUpdatedAt(branch.getUpdatedAt());
        entity.setEntityType(ENTITY_TYPE);
        return entity;
    }

    private Branch mapToDomain(BranchEntity entity) {
        return Branch.builder()
                .id(entity.getId())
                .franchiseId(entity.getFranchiseId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
