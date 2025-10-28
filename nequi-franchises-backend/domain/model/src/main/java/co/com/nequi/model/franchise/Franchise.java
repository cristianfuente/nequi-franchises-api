package co.com.nequi.model.franchise;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Franchise {

    private final String id;
    private final String name;
    private final String country;
    private final Integer totalBranches;

}
