package com.ciphermarket.api.upload.validation;

public record ValidationResult(boolean valid, String sanitizedFileName, String reason) {

    public static ValidationResult ok(String sanitizedFileName) {
        return new ValidationResult(true, sanitizedFileName, null);
    }

    public static ValidationResult reject(String reason) {
        return new ValidationResult(false, null, reason);
    }
}
