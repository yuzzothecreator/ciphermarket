package com.ciphermarket.api.common.exception;

public class TenantIsolationException extends RuntimeException {

    public TenantIsolationException(String message) {
        super(message);
    }
}
