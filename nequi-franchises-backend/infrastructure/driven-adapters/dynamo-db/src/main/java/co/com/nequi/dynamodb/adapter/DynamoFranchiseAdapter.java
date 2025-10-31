package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.FranchiseEntity;
import co.com.nequi.dynamodb.helper.TemplateAdapterOperations;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.franchise.gateways.FranchiseRepository;
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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;

@Repository
public class DynamoFranchiseAdapter extends TemplateAdapterOperations<Franchise, String, FranchiseEntity>
        implements FranchiseRepository {

    private final DynamoDbAsyncTable<FranchiseEntity> franchiseTable;
    private final ObjectMapper mapper;

    public DynamoFranchiseAdapter(DynamoDbEnhancedAsyncClient enhanced,
                                  ObjectMapper mapper,
                                  @Value("${app.dynamo.franchise-table}") String tableName) {
        super(enhanced, mapper, fe -> mapper.map(fe, Franchise.class), tableName);
        this.mapper = mapper;
        this.franchiseTable = enhanced.table(tableName, TableSchema.fromBean(FranchiseEntity.class));
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return super.save(franchise);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return super.getById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(ExceptionMessage.FRANCHISE_NOT_FOUND)));
    }

    @Override
    public Flux<Franchise> findAll() {
        PagePublisher<FranchiseEntity> pub = franchiseTable.scan();
        return Flux.from(pub).flatMapIterable(Page::items)
                .map(fe -> mapper.map(fe, Franchise.class));
    }

    @Override
    public Mono<Franchise> update(Franchise franchise) {
        return Mono.fromFuture(franchiseTable.putItem(mapper.map(franchise, FranchiseEntity.class)))
                .thenReturn(franchise);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(f -> Mono.fromFuture(franchiseTable.deleteItem(Key.builder().partitionValue(id).build())))
                .then();
    }

}
