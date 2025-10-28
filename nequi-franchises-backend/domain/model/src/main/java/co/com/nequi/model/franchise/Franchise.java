package co.com.nequi.model.franchise;

import co.com.nequi.model.recordtype.RecordType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Franchise extends RecordType {

    private final String id;
    private final String name;
    private final String country;
    private final Integer totalBranches;

}
