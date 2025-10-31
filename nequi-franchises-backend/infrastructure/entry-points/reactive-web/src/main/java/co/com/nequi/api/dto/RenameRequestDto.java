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
@Schema(name = "RenameRequest", description = "Petición para renombrar")
public class RenameRequestDto {
    @Schema(description = "Nuevo nombre", example = "Sucursal Norte")
    private String name;
}
