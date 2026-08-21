package com.ciphermarket.api.disclosure.api;

import com.ciphermarket.api.disclosure.dto.CreateDisclosureRequestBody;
import com.ciphermarket.api.disclosure.dto.DisclosureDocumentResponse;
import com.ciphermarket.api.disclosure.dto.DisclosureRequestResponse;
import com.ciphermarket.api.disclosure.service.DisclosureService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/disclosures")
@Tag(name = "Disclosures", description = "Confidential document disclosure (creator side)")
public class OrganisationDisclosureController {

    private final DisclosureService disclosureService;

    public OrganisationDisclosureController(DisclosureService disclosureService) {
        this.disclosureService = disclosureService;
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an encrypted confidential disclosure document")
    public DisclosureDocumentResponse upload(
            @PathVariable UUID organisationId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("title") @NotBlank String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("file") MultipartFile file
    ) {
        return disclosureService.uploadDocument(organisationId, user, title, description, file);
    }

    @GetMapping("/documents")
    @Operation(summary = "List confidential disclosure documents")
    public List<DisclosureDocumentResponse> listDocuments(
            @PathVariable UUID organisationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return disclosureService.listDocuments(organisationId, user);
    }

    @PostMapping("/documents/{documentId}/requests")
    @Operation(summary = "Create a disclosure request for a recipient")
    public DisclosureRequestResponse createRequest(
            @PathVariable UUID organisationId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateDisclosureRequestBody body
    ) {
        return disclosureService.createRequest(organisationId, documentId, user, body);
    }

    @GetMapping("/requests")
    @Operation(summary = "List disclosure requests for the organisation")
    public List<DisclosureRequestResponse> listRequests(
            @PathVariable UUID organisationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return disclosureService.listOrganisationRequests(organisationId, user);
    }

    @PostMapping("/requests/{requestId}/revoke")
    @Operation(summary = "Revoke a disclosure request")
    public DisclosureRequestResponse revoke(
            @PathVariable UUID organisationId,
            @PathVariable UUID requestId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return disclosureService.revokeRequest(organisationId, requestId, user);
    }
}
