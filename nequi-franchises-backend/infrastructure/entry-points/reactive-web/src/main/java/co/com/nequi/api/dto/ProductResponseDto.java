package co.com.nequi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductResponse", description = "Respuesta de producto")
public class ProductResponseDto {
    @Schema(example = "p789")
    private String id;
    @Schema(example = "f123")
    private String franchiseId;
    @Schema(example = "b456")
    private String branchId;
    @Schema(example = "Leche Entera 1L")
    private String name;
    @Schema(example = "42")
    private Integer stock;
    @Schema(example = "1730409600000")
    private Long createdAt;
    @Schema(example = "1730409600000")
    private Long updatedAt;
}
