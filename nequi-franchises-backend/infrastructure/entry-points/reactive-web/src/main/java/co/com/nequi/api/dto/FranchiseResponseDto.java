package co.com.nequi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FranchiseResponseDto {
    private String id;
    private String name;
    private Long createdAt;
    private Long updatedAt;
}
