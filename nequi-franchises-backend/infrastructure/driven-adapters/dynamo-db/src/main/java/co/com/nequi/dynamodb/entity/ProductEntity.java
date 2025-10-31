package co.com.nequi.dynamodb.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class ProductEntity {

    private String id;
    private String entityType;
    private String franchiseId;
    private String branchId;
    private String name;
    private String nameLc;
    private String nameByBranchSortKey;
    private Integer stock;
    private String topSort;
    private Long version;
    private Long createdAt;
    private Long updatedAt;

    public ProductEntity() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute("entityType")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"byFranchise"})
    @DynamoDbAttribute("franchiseId")
    public String getFranchiseId() {
        return franchiseId;
    }

    public void setFranchiseId(String franchiseId) {
        this.franchiseId = franchiseId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"byBranch", "topByBranch", "nameByBranch"})
    @DynamoDbAttribute("branchId")
    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbAttribute("nameLc")
    public String getNameLc() {
        return nameLc;
    }

    public void setNameLc(String nameLc) {
        this.nameLc = nameLc;
    }

    @DynamoDbSecondarySortKey(indexNames = {"nameByBranch"})
    @DynamoDbAttribute("nameByBranchSortKey")
    public String getNameByBranchSortKey() {
        return nameByBranchSortKey;
    }

    public void setNameByBranchSortKey(String nameByBranchSortKey) {
        this.nameByBranchSortKey = nameByBranchSortKey;
    }

    @DynamoDbAttribute("stock")
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @DynamoDbSecondarySortKey(indexNames = {"topByBranch"})
    @DynamoDbAttribute("topSort")
    public String getTopSort() {
        return topSort;
    }

    public void setTopSort(String topSort) {
        this.topSort = topSort;
    }

    @DynamoDbAttribute("version")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
