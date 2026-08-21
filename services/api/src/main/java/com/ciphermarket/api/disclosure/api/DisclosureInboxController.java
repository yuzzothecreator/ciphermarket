package com.ciphermarket.api.disclosure.api;

import com.ciphermarket.api.disclosure.dto.DisclosureRequestResponse;
import com.ciphermarket.api.disclosure.service.DisclosureService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/disclosures/inbox")
@Tag(name = "Disclosure inbox", description = "Recipient confidential disclosure inbox")
public class DisclosureInboxController {

    private final DisclosureService disclosureService;

    public DisclosureInboxController(DisclosureService disclosureService) {
        this.disclosureService = disclosureService;
    }

    @GetMapping
    @Operation(summary = "List incoming disclosure requests")
    public List<DisclosureRequestResponse> inbox(@AuthenticationPrincipal AuthenticatedUser user) {
        return disclosureService.listInbox(user);
    }

    @PostMapping("/{requestId}/accept")
    @Operation(summary = "Accept confidentiality terms")
    public DisclosureRequestResponse accept(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID requestId
    ) {
        return disclosureService.accept(requestId, user);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject a disclosure request")
    public DisclosureRequestResponse reject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID requestId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String note = body != null ? body.get("note") : null;
        return disclosureService.reject(requestId, user, note);
    }

    @GetMapping("/{requestId}/download")
    @Operation(summary = "Download accepted disclosure document")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID requestId
    ) {
        return disclosureService.download(requestId, user);
    }
}
