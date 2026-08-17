package com.ciphermarket.api.organisation.api;

import com.ciphermarket.api.organisation.dto.CreateOrganisationRequest;
import com.ciphermarket.api.organisation.dto.InviteMemberRequest;
import com.ciphermarket.api.organisation.dto.MembershipResponse;
import com.ciphermarket.api.organisation.dto.OrganisationResponse;
import com.ciphermarket.api.organisation.service.OrganisationService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations")
@Tag(name = "Organisations", description = "Organisation and membership management")
public class OrganisationController {

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @PostMapping
    @Operation(summary = "Create a new organisation")
    public OrganisationResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrganisationRequest request
    ) {
        return organisationService.createOrganisation(user, request);
    }

    @GetMapping
    @Operation(summary = "List organisations the current user belongs to")
    public List<OrganisationResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return organisationService.listMyOrganisations(user);
    }

    @GetMapping("/{organisationId}")
    @Operation(summary = "Get organisation by ID")
    public OrganisationResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID organisationId
    ) {
        return organisationService.getOrganisation(user, organisationId);
    }

    @GetMapping("/{organisationId}/members")
    @Operation(summary = "List organisation members")
    public List<MembershipResponse> listMembers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID organisationId
    ) {
        return organisationService.listMembers(user, organisationId);
    }

    @PostMapping("/{organisationId}/members")
    @Operation(summary = "Invite a member to the organisation")
    public MembershipResponse inviteMember(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID organisationId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        return organisationService.inviteMember(user, organisationId, request);
    }
}
