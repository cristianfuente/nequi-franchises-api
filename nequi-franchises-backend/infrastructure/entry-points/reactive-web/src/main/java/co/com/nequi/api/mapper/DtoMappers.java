package co.com.nequi.api.mapper;

import co.com.nequi.api.dto.BranchResponseDto;
import co.com.nequi.api.dto.FranchiseResponseDto;
import co.com.nequi.api.dto.ProductResponseDto;
import co.com.nequi.api.dto.ProductSummaryResponseDto;
import co.com.nequi.model.branch.Branch;
import co.com.nequi.model.franchise.Franchise;
import co.com.nequi.model.product.Product;

public class DtoMappers {

    private DtoMappers() {
    }

    public static FranchiseResponseDto toRes(Franchise f) {
        return FranchiseResponseDto.builder()
                .id(f.getId()).name(f.getName())
                .createdAt(f.getCreatedAt()).updatedAt(f.getUpdatedAt())
                .build();
    }

    public static BranchResponseDto toRes(Branch b) {
        return BranchResponseDto.builder()
                .id(b.getId()).franchiseId(b.getFranchiseId()).name(b.getName())
                .createdAt(b.getCreatedAt()).updatedAt(b.getUpdatedAt())
                .build();
    }

    public static ProductResponseDto toRes(Product p) {
        return ProductResponseDto.builder()
                .id(p.getId()).franchiseId(p.getFranchiseId()).branchId(p.getBranchId())
                .name(p.getName()).stock(p.getStock())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }

    public static ProductSummaryResponseDto toSummary(Product p) {
        return new ProductSummaryResponseDto(p.getId(), p.getName(), p.getStock());
    }

}
