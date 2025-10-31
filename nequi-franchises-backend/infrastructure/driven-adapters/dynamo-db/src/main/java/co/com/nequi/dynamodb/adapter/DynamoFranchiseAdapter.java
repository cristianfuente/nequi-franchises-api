package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.FranchiseEntity;
import co.com.nequi.dynamodb.helper.DynamoPaginationCodec;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
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

@Repository
public class DynamoFranchiseAdapter implements FranchiseRepository {

    private static final String ENTITY_TYPE = "FRANCHISE";
    private static final String GLOBAL_PARTITION_KEY = "FRANCHISE";
    private static final int STREAM_PAGE_SIZE = 100;

    private final DynamoDbAsyncTable<FranchiseEntity> franchiseTable;
    private final ObjectMapper mapper;

    public DynamoFranchiseAdapter(DynamoDbEnhancedAsyncClient enhanced,
                                  ObjectMapper mapper,
                                  @Value("${app.dynamo.core-table}") String tableName) {
        this.mapper = mapper;
        this.franchiseTable = enhanced.table(tableName, TableSchema.fromBean(FranchiseEntity.class));
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return Mono.defer(() -> Mono.fromFuture(franchiseTable.putItem(mapToEntity(franchise)))
                .thenReturn(franchise));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return Mono.defer(() -> Mono.fromFuture(franchiseTable.getItem(r -> r.key(Key.builder()
                                .partitionValue(id)
                                .build())))
                        .flatMap(entity -> entity == null || !ENTITY_TYPE.equals(entity.getEntityType())
                                ? Mono.empty()
                                : Mono.just(mapToDomain(entity))))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.FRANCHISE_NOT_FOUND)));
    }

    @Override
    public Mono<PageResult<Franchise>> findAll(int limit, String exclusiveStartKey) {
        final int pageSize = Math.max(1, limit);
        return Mono.defer(() -> {
            QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(GLOBAL_PARTITION_KEY)))
                    .limit(pageSize)
                    .scanIndexForward(true)
                    .addAttributeToProject("id")
                    .addAttributeToProject("name")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version");

            Map<String, AttributeValue> startKey = DynamoPaginationCodec.decode(exclusiveStartKey);
            if (startKey != null && !startKey.isEmpty()) {
                builder.exclusiveStartKey(startKey);
            }

            return Mono.from(franchiseTable.index("byFranchise").query(builder.build()))
                    .map(page -> {
                        List<Franchise> items = page.items().stream()
                                .filter(item -> ENTITY_TYPE.equals(item.getEntityType()))
                                .map(this::mapToDomain)
                                .toList();
                        String nextCursor = DynamoPaginationCodec.encode(page.lastEvaluatedKey());
                        return PageResult.of(items, nextCursor);
                    });
        });
    }

    @Override
    public Flux<Franchise> streamAll() {
        return Flux.defer(() -> fetchPageRecursively(null));
    }

    private Flux<Franchise> fetchPageRecursively(String cursor) {
        return findAll(STREAM_PAGE_SIZE, cursor)
                .flatMapMany(page -> {
                    Flux<Franchise> items = Flux.fromIterable(page.getItems());
                    if (page.getLastEvaluatedKey() == null) {
                        return items;
                    }
                    return items.concatWith(fetchPageRecursively(page.getLastEvaluatedKey()));
                });
    }

    @Override
    public Mono<Franchise> update(Franchise franchise) {
        return Mono.defer(() -> Mono.fromFuture(franchiseTable.putItem(mapToEntity(franchise)))
                .thenReturn(franchise));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(f -> Mono.fromFuture(franchiseTable.deleteItem(Key.builder().partitionValue(id).build())))
                .then();
    }

    private FranchiseEntity mapToEntity(Franchise franchise) {
        FranchiseEntity entity = new FranchiseEntity();
        entity.setId(franchise.getId());
        entity.setName(franchise.getName());
        entity.setCreatedAt(franchise.getCreatedAt());
        entity.setUpdatedAt(franchise.getUpdatedAt());
        entity.setVersion(null);
        entity.setEntityType(ENTITY_TYPE);
        entity.setFranchiseId(GLOBAL_PARTITION_KEY);
        return entity;
    }

    private Franchise mapToDomain(FranchiseEntity entity) {
        return Franchise.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
