package com.enterprise.layered.common;

/**
 * Domain-specific error codes for this service.
 * (Generic codes live in {@code com.enterprise.common.constant.Constants}.)
 */
public final class ProductErrorCodes {

    private ProductErrorCodes() {
        // constant holder — not instantiable
    }

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_SKU_EXISTS = "PRODUCT_SKU_EXISTS";
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String CATEGORY_NAME_EXISTS = "CATEGORY_NAME_EXISTS";
}
