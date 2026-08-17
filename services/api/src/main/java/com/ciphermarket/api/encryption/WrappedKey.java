package com.ciphermarket.api.encryption;

public record WrappedKey(String ciphertext, String keyVersion) {
}
