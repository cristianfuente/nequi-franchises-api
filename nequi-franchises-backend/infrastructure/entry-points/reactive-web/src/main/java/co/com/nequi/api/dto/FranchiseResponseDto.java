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
@Schema(name = "FranchiseResponse", description = "Respuesta de franquicia")
public class FranchiseResponseDto {
    @Schema(example = "f123")
    private String id;
    @Schema(example = "Franquicia Norte")
    private String name;
    @Schema(example = "1730409600000")
    private Long createdAt;
    @Schema(example = "1730409600000")
    private Long updatedAt;
}
