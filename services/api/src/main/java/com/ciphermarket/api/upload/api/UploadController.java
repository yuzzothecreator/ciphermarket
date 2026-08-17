package com.ciphermarket.api.upload.api;

import com.ciphermarket.api.product.dto.CreateUploadSessionRequest;
import com.ciphermarket.api.product.dto.UploadSessionResponse;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/products/{productId}/uploads")
@Tag(name = "Uploads", description = "Secure product upload pipeline")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create an upload session")
    public UploadSessionResponse createSession(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateUploadSessionRequest request
    ) {
        return uploadService.createSession(organisationId, productId, user, request);
    }

    @PostMapping(value = "/sessions/{sessionId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file to quarantine storage")
    public UploadSessionResponse uploadFile(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file
    ) {
        return uploadService.uploadFile(organisationId, sessionId, user, file);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get upload session status")
    public UploadSessionResponse getSession(
            @PathVariable UUID organisationId,
            @PathVariable UUID productId,
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return uploadService.getSession(organisationId, sessionId, user);
    }
}
