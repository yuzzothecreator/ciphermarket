package com.ciphermarket.api.upload.validation;

import com.ciphermarket.api.config.UploadProperties;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileValidationService {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "msi", "dll", "scr", "ps1", "vbs", "js", "jar", "sh"
    );

    private static final Set<String> BLOCKED_MIMES = Set.of(
            "application/x-msdownload",
            "application/x-dosexec",
            "application/java-archive"
    );

    private final UploadProperties uploadProperties;
    private final Tika tika = new Tika();

    public FileValidationService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public ValidationResult validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return ValidationResult.reject("File name is required");
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ValidationResult.reject("Path traversal in file name is not allowed");
        }
        if (fileName.chars().filter(ch -> ch == '.').count() > 1) {
            String lower = fileName.toLowerCase(Locale.ROOT);
            if (lower.matches(".*\\.(pdf|zip|png|jpg|jpeg)\\.(exe|bat|cmd|com).*")) {
                return ValidationResult.reject("Double extension disguise detected");
            }
        }
        String extension = extensionOf(fileName);
        if (extension.isEmpty()) {
            return ValidationResult.reject("File must have an extension");
        }
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            return ValidationResult.reject("Executable or prohibited file type: " + extension);
        }
        Set<String> allowed = uploadProperties.allowedExtensions().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (!allowed.contains(extension)) {
            return ValidationResult.reject("File extension not allowed: " + extension);
        }
        return ValidationResult.ok(sanitizeFileName(fileName));
    }

    public ValidationResult validateSize(long sizeBytes) {
        if (sizeBytes <= 0) {
            return ValidationResult.reject("Empty files are not allowed");
        }
        if (sizeBytes > uploadProperties.maxFileSizeBytes()) {
            return ValidationResult.reject("File exceeds maximum size of " + uploadProperties.maxFileSizeBytes() + " bytes");
        }
        return ValidationResult.ok(null);
    }

    public String detectMimeType(InputStream input, String fileName) throws IOException {
        return tika.detect(input, fileName);
    }

    public ValidationResult validateMimeType(String detectedMime, String declaredMime) {
        if (detectedMime == null) {
            return ValidationResult.reject("Unable to detect file type");
        }
        if (BLOCKED_MIMES.contains(detectedMime)) {
            return ValidationResult.reject("Prohibited MIME type: " + detectedMime);
        }
        String normalizedDetected = detectedMime.split(";")[0].trim().toLowerCase(Locale.ROOT);
        String normalizedDeclared = declaredMime.split(";")[0].trim().toLowerCase(Locale.ROOT);
        if (!mimeCompatible(normalizedDeclared, normalizedDetected)) {
            return ValidationResult.reject(
                    "MIME type mismatch: declared " + normalizedDeclared + ", detected " + normalizedDetected
            );
        }
        return ValidationResult.ok(null);
    }

    private boolean mimeCompatible(String declared, String detected) {
        if (declared.equals(detected)) {
            return true;
        }
        if (declared.equals("application/zip") && detected.equals("application/x-zip-compressed")) {
            return true;
        }
        if (declared.startsWith("image/") && detected.startsWith("image/")) {
            return declared.equals(detected);
        }
        return false;
    }

    public String sanitizeFileName(String fileName) {
        String base = Paths.get(fileName).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
