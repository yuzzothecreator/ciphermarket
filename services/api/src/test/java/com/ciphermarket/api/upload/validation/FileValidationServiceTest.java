package com.ciphermarket.api.upload.validation;

import com.ciphermarket.api.config.UploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileValidationServiceTest {

    private FileValidationService service;

    @BeforeEach
    void setUp() {
        service = new FileValidationService(new UploadProperties(524288000L, 30, List.of("pdf", "zip", "png")));
    }

    @Test
    void rejectsPathTraversal() {
        ValidationResult result = service.validateFileName("../etc/passwd");
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsBlockedExtension() {
        ValidationResult result = service.validateFileName("malware.exe");
        assertThat(result.valid()).isFalse();
    }

    @Test
    void acceptsValidPdfName() {
        ValidationResult result = service.validateFileName("my-ebook.pdf");
        assertThat(result.valid()).isTrue();
        assertThat(result.sanitizedFileName()).isEqualTo("my-ebook.pdf");
    }

    @Test
    void rejectsOversizedFile() {
        ValidationResult result = service.validateSize(600_000_000L);
        assertThat(result.valid()).isFalse();
    }
}
