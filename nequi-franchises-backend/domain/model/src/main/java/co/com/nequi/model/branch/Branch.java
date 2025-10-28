package co.com.nequi.model.branch;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Branch {

    private final String id;
    private final String franchiseId;
    private final String name;
    private final Long createdAt;
    private final Long updatedAt;

}
