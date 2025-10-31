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
@Schema(name = "FranchiseCreateRequest", description = "Petición para crear franquicia")
public class FranchiseCreateRequestDto {
    @Schema(description = "Nombre de la franquicia", example = "Franquicia Norte")
    private String name;
}
