package com.enterprise.customer.common;

/**
 * Domain-specific error codes for the customer service.
 * (Generic codes live in {@code com.enterprise.common.constant.Constants}.)
 */
public final class CustomerErrorCodes {

    private CustomerErrorCodes() {
        // constant holder — not instantiable
    }

    public static final String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    public static final String CUSTOMER_EMAIL_EXISTS = "CUSTOMER_EMAIL_EXISTS";
}
