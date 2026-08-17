package com.ciphermarket.api.encryption;

import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptionProvider {

    EncryptedPayload encryptStream(InputStream plaintext, String associatedData);

    void decryptStream(
            InputStream ciphertext,
            byte[] nonce,
            WrappedKey wrappedKey,
            String associatedData,
            OutputStream output
    );

    byte[] generateDataKey();
}
