package com.ciphermarket.api.encryption;

import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.subtle.AesGcmJce;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Component
public class TinkStreamingEncryption {

    static {
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to register Tink AEAD", ex);
        }
    }

    public EncryptedPayload encrypt(
            InputStream plaintext,
            byte[] dek,
            String associatedData,
            WrappedKey wrappedKey
    ) {
        try {
            AesGcmJce aead = new AesGcmJce(dek);
            byte[] plainBytes = plaintext.readAllBytes();
            byte[] aad = associatedData.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = aead.encrypt(plainBytes, aad);
            return new EncryptedPayload(ciphertext, new byte[0], wrappedKey);
        } catch (Exception ex) {
            throw new EncryptionException("Stream encryption failed", ex);
        }
    }

    public void decrypt(
            InputStream ciphertextStream,
            byte[] nonce,
            byte[] dek,
            String associatedData,
            OutputStream output
    ) {
        try {
            AesGcmJce aead = new AesGcmJce(dek);
            byte[] ciphertext = ciphertextStream.readAllBytes();
            byte[] aad = associatedData.getBytes(StandardCharsets.UTF_8);
            byte[] plain = aead.decrypt(ciphertext, aad);
            output.write(plain);
        } catch (Exception ex) {
            throw new EncryptionException("Stream decryption failed", ex);
        }
    }
}
