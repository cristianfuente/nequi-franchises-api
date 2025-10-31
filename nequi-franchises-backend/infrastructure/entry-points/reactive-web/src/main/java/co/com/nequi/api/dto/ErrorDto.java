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
@Schema(name = "Error", description = "Error estándar")
public class ErrorDto {
    @Schema(example = "VALIDATION")
    private String code;
    @Schema(example = "Campo name es obligatorio")
    private String message;
    @Schema(example = "1730409600000")
    private Long timestamp;
}
