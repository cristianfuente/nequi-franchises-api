package co.com.nequi.usecase.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionMessage {

    SAME_NAME("The new name is the same as the current name"),
    FRANCHISE_NOT_FOUND("Franchise not found"),
    FRANCHISE_NAME_REQUIRED("Franchise name is required"),
    BRANCH_NAME_REQUIRED("Branch name is required"),
    BRANCH_NOT_FOUND("Branch not found"),
    PRODUCT_NAME_REQUIRED("Product name is required"),
    PRODUCT_STOCK_INVALID("Stock must be greater than or equal to 0"),
    PRODUCT_NOT_FOUND("Product not found"),
    NO_PRODUCTS_IN_BRANCH("No products found in branch");

    private final String message;

}
