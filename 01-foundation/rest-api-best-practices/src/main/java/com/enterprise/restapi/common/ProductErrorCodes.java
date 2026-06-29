package com.enterprise.restapi.common;

/**
 * Domain-specific error codes.
 */
public final class ProductErrorCodes {

    private ProductErrorCodes() {
        // constant holder — not instantiable
    }

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_SKU_EXISTS = "PRODUCT_SKU_EXISTS";
}
