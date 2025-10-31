package co.com.nequi.dynamodb.helper;

import co.com.nequi.dynamodb.DynamoProductAdapter;
import co.com.nequi.dynamodb.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TemplateAdapterOperationsTest {

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DynamoDbAsyncTable<ProductEntity> customerTable;

    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(dynamoDbEnhancedAsyncClient.table("table_name", TableSchema.fromBean(ProductEntity.class)))
                .thenReturn(customerTable);

        productEntity = new ProductEntity();
        productEntity.setId("id");
        productEntity.setAtr1("atr1");
    }

    @Test
    void modelEntityPropertiesMustNotBeNull() {
        ProductEntity productEntityUnderTest = new ProductEntity("id", "atr1");

        assertNotNull(productEntityUnderTest.getId());
        assertNotNull(productEntityUnderTest.getAtr1());
    }

    @Test
    void testSave() {
        when(customerTable.putItem(productEntity)).thenReturn(CompletableFuture.runAsync(() -> {
        }));
        when(mapper.map(productEntity, ProductEntity.class)).thenReturn(productEntity);

        DynamoProductAdapter dynamoProductAdapter =
                new DynamoProductAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoProductAdapter.save(productEntity))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetById() {
        String id = "id";

        when(customerTable.getItem(
                Key.builder().partitionValue(AttributeValue.builder().s(id).build()).build()))
                .thenReturn(CompletableFuture.completedFuture(productEntity));
        when(mapper.map(productEntity, Object.class)).thenReturn("value");

        DynamoProductAdapter dynamoProductAdapter =
                new DynamoProductAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoProductAdapter.getById("id"))
                .expectNext("value")
                .verifyComplete();
    }

    @Test
    void testDelete() {
        when(mapper.map(productEntity, ProductEntity.class)).thenReturn(productEntity);
        when(mapper.map(productEntity, Object.class)).thenReturn("value");

        when(customerTable.deleteItem(productEntity))
                .thenReturn(CompletableFuture.completedFuture(productEntity));

        DynamoProductAdapter dynamoProductAdapter =
                new DynamoProductAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoProductAdapter.delete(productEntity))
                .expectNext("value")
                .verifyComplete();
    }
}