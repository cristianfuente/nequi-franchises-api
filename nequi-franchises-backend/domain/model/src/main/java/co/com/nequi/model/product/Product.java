package co.com.nequi.model.product;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product {

    private final String name;
    private final String branchId;
    private final String franchiseId;
    private final BigDecimal stock;
    private final Long createdAt;
    private final Long updatedAt;

}
