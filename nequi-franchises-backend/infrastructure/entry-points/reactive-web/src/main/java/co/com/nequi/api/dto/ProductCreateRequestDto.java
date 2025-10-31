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
@Schema(name = "ProductCreateRequest", description = "Petición para crear producto")
public class ProductCreateRequestDto {
    @Schema(example = "Leche Entera 1L")
    private String name;
    @Schema(example = "10", minimum = "0")
    private Integer stock;
}
