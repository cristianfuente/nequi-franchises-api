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
@Schema(name = "TopProductItemResponse", description = "Top producto por sucursal")
public class TopProductItemResponseDto {
    @Schema(example = "b456")
    private String branchId;
    @Schema(example = "Sucursal Centro")
    private String branchName;
    private ProductSummaryResponseDto product;
}
