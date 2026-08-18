package com.ciphermarket.api.delivery.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ciphermarket.api.delivery.signing.LicenceSigningService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class SourceManifestService {

    private final LicenceSigningService signingService;
    private final ObjectMapper objectMapper;

    public SourceManifestService(LicenceSigningService signingService, ObjectMapper objectMapper) {
        this.signingService = signingService;
        this.objectMapper = objectMapper;
    }

    public byte[] attachManifest(byte[] archiveBytes, String fileName, String sha256, String buyerLabel) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(output);
             ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(archiveBytes))) {

            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                zipOut.putNextEntry(new ZipEntry(entry.getName()));
                zipIn.transferTo(zipOut);
                zipOut.closeEntry();
            }

            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("fileName", fileName);
            manifest.put("sha256", sha256);
            manifest.put("buyer", buyerLabel);
            manifest.put("algorithm", "Ed25519");
            byte[] manifestJson = objectMapper.writeValueAsBytes(manifest);
            String signature = signingService.signRaw(manifestJson);

            zipOut.putNextEntry(new ZipEntry("ciphermarket-manifest.json"));
            zipOut.write(manifestJson);
            zipOut.closeEntry();

            zipOut.putNextEntry(new ZipEntry("ciphermarket-manifest.sig"));
            zipOut.write(signature.getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();

            zipOut.finish();
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to attach source manifest", e);
        }
    }
}
