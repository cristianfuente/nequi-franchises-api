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
@Schema(name = "ChangeStockRequest", description = "Cambio de stock por delta")
public class ChangeStockRequestDto {
    @Schema(description = "Delta de stock, puede ser negativo", example = "-5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer delta;
}
