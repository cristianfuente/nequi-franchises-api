package co.com.nequi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchResponseDto {
    private String id;
    private String franchiseId;
    private String name;
    private Long createdAt;
    private Long updatedAt;
}
