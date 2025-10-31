package co.com.nequi.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product {

    private final String id;
    private final String franchiseId;
    private final String branchId;
    private final String name;
    private final Integer stock;
    private final Long createdAt;
    private final Long updatedAt;

}
