package co.com.nequi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private String id;
    private String franchiseId;
    private String branchId;
    private String name;
    private Integer stock;
    private Long createdAt;
    private Long updatedAt;
}
