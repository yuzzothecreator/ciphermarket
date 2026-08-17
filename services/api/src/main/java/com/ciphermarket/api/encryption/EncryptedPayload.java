package com.ciphermarket.api.encryption;

public record EncryptedPayload(
        byte[] ciphertext,
        byte[] nonce,
        WrappedKey wrappedKey
) {
}
