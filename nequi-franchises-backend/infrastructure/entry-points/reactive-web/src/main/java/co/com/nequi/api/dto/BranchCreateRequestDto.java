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
@Schema(name = "BranchCreateRequest", description = "Petición para crear una sucursal")
public class BranchCreateRequestDto {
    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro")
    private String name;
}
