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
@Schema(name = "BranchPageResponse", description = "Página de sucursales")
public class BranchPageResponseDto {

    @Schema(description = "Listado paginado")
    private List<BranchResponseDto> items;

    @Schema(description = "Cursor para la siguiente página", nullable = true)
    private String lastEvaluatedKey;
}
