package com.ciphermarket.api.encryption;

import com.ciphermarket.api.config.VaultProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Component
public class VaultTransitEncryptionProvider implements EncryptionProvider {

    private final RestClient vaultClient;
    private final VaultProperties properties;
    private final TinkStreamingEncryption tinkEncryption;

    public VaultTransitEncryptionProvider(
            VaultProperties properties,
            TinkStreamingEncryption tinkEncryption
    ) {
        this.properties = properties;
        this.tinkEncryption = tinkEncryption;
        this.vaultClient = RestClient.builder()
                .baseUrl(properties.address())
                .defaultHeader("X-Vault-Token", properties.token())
                .build();
    }

    @Override
    public EncryptedPayload encryptStream(java.io.InputStream plaintext, String associatedData) {
        byte[] dek = generateDataKey();
        WrappedKey wrapped = wrapKey(dek);
        try {
            return tinkEncryption.encrypt(plaintext, dek, associatedData, wrapped);
        } finally {
            java.util.Arrays.fill(dek, (byte) 0);
        }
    }

    @Override
    public void decryptStream(
            java.io.InputStream ciphertext,
            byte[] nonce,
            WrappedKey wrappedKey,
            String associatedData,
            java.io.OutputStream output
    ) {
        byte[] dek = unwrapKey(wrappedKey.ciphertext(), wrappedKey.keyVersion());
        try {
            tinkEncryption.decrypt(ciphertext, nonce, dek, associatedData, output);
        } finally {
            java.util.Arrays.fill(dek, (byte) 0);
        }
    }

    @Override
    public byte[] generateDataKey() {
        byte[] dek = new byte[32];
        new java.security.SecureRandom().nextBytes(dek);
        return dek;
    }

    public WrappedKey wrapKey(byte[] dek) {
        String path = "/v1/transit/encrypt/" + properties.transitKey();
        Map<String, Object> body = Map.of("plaintext", Base64.getEncoder().encodeToString(dek));
        JsonNode response = vaultClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("data")) {
            throw new EncryptionException("Vault transit encrypt returned empty response");
        }
        String ciphertext = response.get("data").get("ciphertext").asText();
        String keyVersion = extractKeyVersion(ciphertext);
        return new WrappedKey(ciphertext, keyVersion);
    }

    public byte[] unwrapKey(String wrappedDek, String keyVersion) {
        String path = "/v1/transit/decrypt/" + properties.transitKey();
        Map<String, Object> body = Map.of("ciphertext", wrappedDek);
        JsonNode response = vaultClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("data")) {
            throw new EncryptionException("Vault transit decrypt returned empty response");
        }
        String plaintext = response.get("data").get("plaintext").asText();
        return Base64.getDecoder().decode(plaintext);
    }

    private String extractKeyVersion(String ciphertext) {
        int idx = ciphertext.lastIndexOf(':');
        if (idx > 0 && ciphertext.startsWith("vault:v")) {
            return ciphertext.substring(0, idx);
        }
        return "vault:v1";
    }
}
