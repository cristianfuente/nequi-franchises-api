package co.com.nequi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProductPageResponse", description = "Página de productos")
public class ProductPageResponseDto {

    @Schema(description = "Listado paginado")
    private List<ProductResponseDto> items;

    @Schema(description = "Cursor para la siguiente página", nullable = true)
    private String lastEvaluatedKey;
}
