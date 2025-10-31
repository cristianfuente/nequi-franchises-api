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
@Schema(name = "PagedResponse", description = "Respuesta paginada")
public class PagedResponseDto<T> {

    @Schema(description = "Listado de elementos de la página actual")
    private List<T> items;

    @Schema(description = "Cursor para recuperar la siguiente página", nullable = true)
    private String lastEvaluatedKey;
}
