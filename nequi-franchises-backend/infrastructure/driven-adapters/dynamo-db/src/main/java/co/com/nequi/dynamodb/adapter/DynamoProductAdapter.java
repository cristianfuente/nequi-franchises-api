package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.ProductEntity;
import co.com.nequi.dynamodb.helper.TemplateAdapterOperations;
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
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;
import software.amazon.awssdk.services.dynamodb.model.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;


@Repository
public class DynamoProductAdapter extends TemplateAdapterOperations<Product, String, ProductEntity>
        implements ProductRepository {

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final DynamoDbAsyncTable<ProductEntity> productTable;
    private final String tableName;

    public DynamoProductAdapter(DynamoDbEnhancedAsyncClient enhanced,
                                DynamoDbAsyncClient dynamoDbAsyncClient,
                                ObjectMapper mapper,
                                @Value("${app.dynamo.product-table}") String tableName) {
        super(enhanced, mapper, pe -> mapper.map(pe, Product.class), tableName,
                "byBranch");
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.tableName = tableName;
        this.productTable = enhanced.table(tableName, TableSchema.fromBean(ProductEntity.class));
    }

    @Override
    public Mono<Product> save(Product product) {
        return super.save(product);
    }

    @Override
    public Mono<Product> update(Product product) {
        return Mono.fromFuture(productTable.putItem(mapper.map(product, ProductEntity.class)))
                .thenReturn(product);
    }

    @Override
    public Mono<Product> findById(String id) {
        return super.getById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)));
    }

    @Override
    public Mono<Product> findByIdAndBranchId(String id, String branchId) {
        return findById(id)
                .filter(p -> Objects.equals(p.getBranchId(), branchId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)));
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
    public Flux<Product> findAll() {
        PagePublisher<ProductEntity> publisher = productTable.scan();
        return Flux.from(publisher)
                .flatMapIterable(Page::items)
                .map(pe -> mapper.map(pe, Product.class));
    }

    @Override
    public Flux<Product> findByBranchId(String branchId) {
        var index = productTable.index("byBranch");
        var req = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(branchId)))
                .build();
        SdkPublisher<Page<ProductEntity>> pub = index.query(req);
        return Flux.from(pub)
                .flatMapIterable(Page::items)
                .map(pe -> mapper.map(pe, Product.class));
    }

    @Override
    public Flux<Product> findByFranchiseId(String franchiseId) {
        var index = productTable.index("byFranchise");
        var req = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(franchiseId)))
                .build();
        SdkPublisher<Page<ProductEntity>> pub = index.query(req);
        return Flux.from(pub)
                .flatMapIterable(Page::items)
                .map(pe -> mapper.map(pe, Product.class));
    }

    @Override
    public Mono<Product> findTopByBranchId(String branchId) {
        var index = productTable.index("topByBranch");
        var req = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(branchId)))
                .limit(1)
                .build();
        return Mono.from(index.query(req))
                .flatMap(page -> page.items().isEmpty() ? Mono.empty()
                        : Mono.just(mapper.map(page.items().get(0), Product.class)));
    }


    @Override
    public Mono<Product> changeStockAtomic(String productId, int delta, String idempotencyKey) {
        return Mono.defer(() -> {
            Map<String, AttributeValue> key = Map.of("id", AttributeValue.fromS(productId));
            long now = System.currentTimeMillis();

            Map<String, AttributeValue> values = new HashMap<>();
            values.put(":d", AttributeValue.fromN(String.valueOf(delta)));
            values.put(":zero", AttributeValue.fromN("0"));
            values.put(":one", AttributeValue.fromN("1"));
            values.put(":z", AttributeValue.fromN("0"));
            values.put(":now", AttributeValue.fromN(String.valueOf(now)));

            Update update = Update.builder()
                    .tableName(tableName)
                    .key(key)
                    .conditionExpression("attribute_exists(id) AND (stock + :d) >= :zero")
                    .updateExpression("SET stock = stock + :d, " +
                            "version = if_not_exists(version, :z) + :one, " +
                            "updatedAt = :now")
                    .expressionAttributeValues(values)
                    .build();

            TransactWriteItemsRequest tx = TransactWriteItemsRequest.builder()
                    .clientRequestToken(idempotencyKey) // idempotencia a nivel de API
                    .transactItems(TransactWriteItem.builder().update(update).build())
                    .build();

            return Mono.fromFuture(dynamoDbAsyncClient.transactWriteItems(tx))
                    .then(Mono.fromFuture(productTable.getItem(r -> r.key(Key.builder().partitionValue(productId).build()))))
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                    .map(pe -> mapper.map(pe, Product.class))
                    .onErrorMap(this::mapTxErrors);
        });
    }

    private Throwable mapTxErrors(Throwable t) {
        Throwable cause = (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
        if (cause instanceof TransactionCanceledException) {
            return new ValidationException(ExceptionMessage.PRODUCT_STOCK_INVALID);
        }
        if (cause instanceof ConditionalCheckFailedException) {
            return new ValidationException(ExceptionMessage.PRODUCT_STOCK_INVALID);
        }
        if (cause instanceof SdkException) {
            return cause;
        }
        return cause;
    }

}
