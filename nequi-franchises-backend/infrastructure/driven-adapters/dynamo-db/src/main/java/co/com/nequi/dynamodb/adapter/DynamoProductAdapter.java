package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.ProductEntity;
import co.com.nequi.dynamodb.helper.DynamoPaginationCodec;
import co.com.nequi.model.pagination.PageResult;
import co.com.nequi.model.product.Product;
import co.com.nequi.model.product.gateways.ProductRepository;
import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ResourceNotFoundException;
import co.com.nequi.usecase.exception.ValidationException;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;
import software.amazon.awssdk.services.dynamodb.model.Update;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;

@Repository
public class DynamoProductAdapter implements ProductRepository {

    private static final String ENTITY_TYPE = "PRODUCT";
    private static final long TOP_SORT_BASE = 999_999_999_999L;
    private static final int STREAM_PAGE_SIZE = 100;

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final DynamoDbAsyncTable<ProductEntity> productTable;
    private final ObjectMapper mapper;
    private final String tableName;

    public DynamoProductAdapter(DynamoDbEnhancedAsyncClient enhanced,
                                DynamoDbAsyncClient dynamoDbAsyncClient,
                                ObjectMapper mapper,
                                @Value("${app.dynamo.core-table}") String tableName) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.mapper = mapper;
        this.tableName = tableName;
        this.productTable = enhanced.table(tableName, TableSchema.fromBean(ProductEntity.class));
    }

    @Override
    public Mono<Product> save(Product product) {
        return Mono.defer(() -> Mono.fromFuture(productTable.putItem(mapToEntity(product)))
                .thenReturn(product));
    }

    @Override
    public Mono<Product> update(Product product) {
        return Mono.defer(() -> Mono.fromFuture(productTable.putItem(mapToEntity(product)))
                .thenReturn(product));
    }

    @Override
    public Mono<Product> findById(String id) {
        return Mono.defer(() -> Mono.fromFuture(productTable.getItem(r -> r.key(Key.builder()
                                .partitionValue(id)
                                .build())))
                        .flatMap(entity -> entity == null || !ENTITY_TYPE.equals(entity.getEntityType())
                                ? Mono.empty()
                                : Mono.just(mapToDomain(entity))))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)));
    }

    @Override
    public Mono<Product> findByIdAndBranchId(String id, String branchId) {
        return findById(id)
                .filter(product -> Objects.equals(product.getBranchId(), branchId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)));
    }

    @Override
    public Mono<PageResult<Product>> findByBranchId(String branchId, int limit, String exclusiveStartKey) {
        int pageSize = Math.max(1, limit);
        return Mono.defer(() -> {
            QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(branchId)))
                    .limit(pageSize)
                    .scanIndexForward(true)
                    .addAttributeToProject("id")
                    .addAttributeToProject("branchId")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("name")
                    .addAttributeToProject("nameLc")
                    .addAttributeToProject("stock")
                    .addAttributeToProject("topSort")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version");

            Map<String, AttributeValue> startKey = DynamoPaginationCodec.decode(exclusiveStartKey);
            if (startKey != null && !startKey.isEmpty()) {
                builder.exclusiveStartKey(startKey);
            }

            return Mono.from(productTable.index("byBranch").query(builder.build()))
                    .map(page -> {
                        List<Product> items = page.items().stream()
                                .filter(entity -> ENTITY_TYPE.equals(entity.getEntityType()))
                                .map(this::mapToDomain)
                                .toList();
                        String nextCursor = DynamoPaginationCodec.encode(page.lastEvaluatedKey());
                        return PageResult.of(items, nextCursor);
                    });
        });
    }

    @Override
    public Mono<PageResult<Product>> findByFranchiseId(String franchiseId, int limit, String exclusiveStartKey) {
        int pageSize = Math.max(1, limit);
        return Mono.defer(() -> {
            QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(franchiseId)))
                    .limit(pageSize)
                    .scanIndexForward(true)
                    .addAttributeToProject("id")
                    .addAttributeToProject("branchId")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("name")
                    .addAttributeToProject("stock")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version");

            Map<String, AttributeValue> startKey = DynamoPaginationCodec.decode(exclusiveStartKey);
            if (startKey != null && !startKey.isEmpty()) {
                builder.exclusiveStartKey(startKey);
            }

            return Mono.from(productTable.index("byFranchise").query(builder.build()))
                    .map(page -> {
                        List<Product> items = page.items().stream()
                                .filter(entity -> ENTITY_TYPE.equals(entity.getEntityType()))
                                .map(this::mapToDomain)
                                .toList();
                        String nextCursor = DynamoPaginationCodec.encode(page.lastEvaluatedKey());
                        return PageResult.of(items, nextCursor);
                    });
        });
    }

    @Override
    public Mono<PageResult<Product>> searchByName(String branchId, String prefix, int limit, String exclusiveStartKey) {
        int pageSize = Math.max(1, limit);
        String normalizedPrefix = normalizeName(prefix);
        if (normalizedPrefix.isBlank()) {
            return Mono.error(new ValidationException(ExceptionMessage.PRODUCT_NAME_REQUIRED));
        }
        return Mono.defer(() -> {
            QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.sortBeginsWith(k -> k.partitionValue(branchId)
                            .sortValue(buildNamePrefix(normalizedPrefix))))
                    .limit(pageSize)
                    .addAttributeToProject("id")
                    .addAttributeToProject("branchId")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("name")
                    .addAttributeToProject("nameLc")
                    .addAttributeToProject("stock")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version");

            Map<String, AttributeValue> startKey = DynamoPaginationCodec.decode(exclusiveStartKey);
            if (startKey != null && !startKey.isEmpty()) {
                builder.exclusiveStartKey(startKey);
            }

            return Mono.from(productTable.index("nameByBranch").query(builder.build()))
                    .map(page -> {
                        List<Product> items = page.items().stream()
                                .filter(entity -> ENTITY_TYPE.equals(entity.getEntityType()))
                                .map(this::mapToDomain)
                                .toList();
                        String nextCursor = DynamoPaginationCodec.encode(page.lastEvaluatedKey());
                        return PageResult.of(items, nextCursor);
                    });
        });
    }

    @Override
    public Flux<Product> streamByBranch(String branchId) {
        return Flux.defer(() -> fetchProductsByBranch(branchId, null));
    }

    private Flux<Product> fetchProductsByBranch(String branchId, String cursor) {
        return findByBranchId(branchId, STREAM_PAGE_SIZE, cursor)
                .flatMapMany(page -> {
                    Flux<Product> items = Flux.fromIterable(page.getItems());
                    if (page.getLastEvaluatedKey() == null) {
                        return items;
                    }
                    return items.concatWith(fetchProductsByBranch(branchId, page.getLastEvaluatedKey()));
                });
    }

    @Override
    public Mono<Product> changeStockAtomic(String productId, int delta, String idempotencyKey) {
        return Mono.defer(() -> {
            Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(productId).build());
            long now = Instant.now().toEpochMilli();

            Map<String, AttributeValue> values = new HashMap<>();
            values.put(":d", AttributeValue.fromN(String.valueOf(delta)));
            values.put(":one", AttributeValue.fromN("1"));
            values.put(":z", AttributeValue.fromN("0"));
            values.put(":now", AttributeValue.fromN(String.valueOf(now)));
            String condition = "attribute_exists(id)";
            if (delta < 0) {
                values.put(":requiredStock", AttributeValue.fromN(String.valueOf(Math.abs(delta))));
                condition = condition + " AND stock >= :requiredStock";
            }

            Update update = Update.builder()
                    .tableName(tableName)
                    .key(key)
                    .conditionExpression(condition)
                    .updateExpression("SET stock = stock + :d, version = if_not_exists(version, :z) + :one, updatedAt = :now")
                    .expressionAttributeValues(values)
                    .build();

            TransactWriteItemsRequest tx = TransactWriteItemsRequest.builder()
                    .clientRequestToken(idempotencyKey)
                    .transactItems(TransactWriteItem.builder().update(update).build())
                    .build();

            return Mono.fromFuture(dynamoDbAsyncClient.transactWriteItems(tx))
                    .then(Mono.fromFuture(productTable.getItem(r -> r.key(Key.builder()
                            .partitionValue(productId)
                            .build()))))
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                    .flatMap(entity -> {
                        if (!ENTITY_TYPE.equals(entity.getEntityType())) {
                            return Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND));
                        }
                        applyDerivedFields(entity);
                        return Mono.fromFuture(productTable.updateItem(entity))
                                .thenReturn(mapToDomain(entity));
                    })
                    .onErrorMap(this::mapTxErrors);
        });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(p -> Mono.fromFuture(productTable.deleteItem(Key.builder().partitionValue(id).build())))
                .then();
    }

    @Override
    public Mono<Void> deleteByBranchAndId(String branchId, String productId) {
        return findByIdAndBranchId(productId, branchId)
                .flatMap(p -> Mono.fromFuture(productTable.deleteItem(Key.builder().partitionValue(productId).build())))
                .then();
    }

    @Override
    public Mono<Product> findTopByBranchId(String branchId) {
        return Mono.defer(() -> {
            QueryEnhancedRequest req = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(branchId)))
                    .limit(1)
                    .addAttributeToProject("id")
                    .addAttributeToProject("branchId")
                    .addAttributeToProject("franchiseId")
                    .addAttributeToProject("entityType")
                    .addAttributeToProject("name")
                    .addAttributeToProject("stock")
                    .addAttributeToProject("createdAt")
                    .addAttributeToProject("updatedAt")
                    .addAttributeToProject("version")
                    .scanIndexForward(true)
                    .build();

            SdkPublisher<Page<ProductEntity>> publisher = productTable.index("topByBranch").query(req);
            return Mono.from(publisher)
                    .flatMap(page -> page.items().stream()
                            .filter(entity -> ENTITY_TYPE.equals(entity.getEntityType()))
                            .findFirst()
                            .map(item -> Mono.just(mapToDomain(item)))
                            .orElse(Mono.empty()));
        });
    }

    private ProductEntity mapToEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setFranchiseId(product.getFranchiseId());
        entity.setBranchId(product.getBranchId());
        entity.setName(product.getName());
        entity.setStock(product.getStock());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        entity.setVersion(null);
        applyDerivedFields(entity);
        return entity;
    }

    private void applyDerivedFields(ProductEntity entity) {
        entity.setEntityType(ENTITY_TYPE);
        String normalizedName = normalizeName(entity.getName());
        entity.setNameLc(normalizedName);
        if (entity.getId() != null && entity.getBranchId() != null && normalizedName != null) {
            entity.setNameByBranchSortKey(buildNameSortKey(normalizedName, entity.getId()));
        }
        entity.setTopSort(computeTopSort(entity.getStock(), entity.getId()));
    }

    private Product mapToDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .franchiseId(entity.getFranchiseId())
                .branchId(entity.getBranchId())
                .name(entity.getName())
                .stock(entity.getStock())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String buildNameSortKey(String normalizedName, String productId) {
        return "NAME#" + normalizedName + "#PROD#" + productId;
    }

    private String buildNamePrefix(String normalizedPrefix) {
        return "NAME#" + normalizedPrefix;
    }

    private String computeTopSort(Integer stock, String productId) {
        if (productId == null) {
            return null;
        }
        long safeStock = stock == null ? 0L : Math.max(0, stock.longValue());
        long score = TOP_SORT_BASE - safeStock;
        return "RANK#" + String.format("%012d", score) + "#PROD#" + productId;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    private Throwable mapTxErrors(Throwable t) {
        Throwable cause = (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
        if (cause instanceof TransactionCanceledException || cause instanceof ConditionalCheckFailedException) {
            return new ValidationException(ExceptionMessage.PRODUCT_STOCK_INVALID);
        }
        if (cause instanceof SdkException) {
            return cause;
        }
        return cause;
    }
}
