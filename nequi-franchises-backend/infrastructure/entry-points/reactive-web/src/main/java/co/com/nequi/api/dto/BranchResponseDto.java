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
@Schema(name = "BranchResponse", description = "Respuesta de sucursal")
public class BranchResponseDto {
    @Schema(example = "b123")
    private String id;
    @Schema(example = "f123")
    private String franchiseId;
    @Schema(example = "Sucursal Centro")
    private String name;
    @Schema(example = "1730409600000")
    private Long createdAt;
    @Schema(example = "1730409600000")
    private Long updatedAt;
}
