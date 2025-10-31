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
@Schema(name = "ProductSummaryResponse", description = "Resumen de producto")
public class ProductSummaryResponseDto {
    @Schema(example = "p789")
    private String id;
    @Schema(example = "Leche Entera 1L")
    private String name;
    @Schema(example = "42")
    private Integer stock;
}
