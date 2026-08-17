package com.ciphermarket.api.organisation.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.MembershipStatus;
import com.ciphermarket.api.common.enums.OrganisationRole;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.common.exception.TenantIsolationException;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.repository.UserProfileRepository;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.organisation.domain.Organisation;
import com.ciphermarket.api.organisation.domain.OrganisationMembership;
import com.ciphermarket.api.organisation.dto.CreateOrganisationRequest;
import com.ciphermarket.api.organisation.dto.InviteMemberRequest;
import com.ciphermarket.api.organisation.dto.MembershipResponse;
import com.ciphermarket.api.organisation.dto.OrganisationResponse;
import com.ciphermarket.api.organisation.repository.OrganisationMembershipRepository;
import com.ciphermarket.api.organisation.repository.OrganisationRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMembershipRepository membershipRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    private final AuditService auditService;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            OrganisationMembershipRepository membershipRepository,
            UserProfileRepository userProfileRepository,
            UserProfileService userProfileService,
            AuditService auditService
    ) {
        this.organisationRepository = organisationRepository;
        this.membershipRepository = membershipRepository;
        this.userProfileRepository = userProfileRepository;
        this.userProfileService = userProfileService;
        this.auditService = auditService;
    }

    @Transactional
    public OrganisationResponse createOrganisation(AuthenticatedUser user, CreateOrganisationRequest request) {
        if (!user.hasRole("creator") && !user.hasRole("marketplace_admin")) {
            throw new AccessDeniedException("Creator role required to create an organisation");
        }

        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());

        if (organisationRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Organisation slug already exists");
        }

        Organisation organisation = new Organisation(
                request.name(),
                request.slug(),
                request.description(),
                profile.getId()
        );
        organisation = organisationRepository.save(organisation);

        OrganisationMembership ownerMembership = new OrganisationMembership(
                organisation.getId(),
                profile.getId(),
                OrganisationRole.OWNER,
                null
        );
        membershipRepository.save(ownerMembership);

        auditService.record(
                organisation.getId(),
                profile.getId(),
                user.keycloakSub(),
                "ORGANISATION_CREATED",
                "organisation",
                organisation.getId(),
                null,
                Map.of("name", organisation.getName(), "slug", organisation.getSlug())
        );

        return OrganisationResponse.from(organisation);
    }

    @Transactional(readOnly = true)
    public List<OrganisationResponse> listMyOrganisations(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return membershipRepository.findByUserIdAndStatus(profile.getId(), MembershipStatus.ACTIVE).stream()
                .map(m -> organisationRepository.findById(m.getOrganisationId())
                        .map(OrganisationResponse::from)
                        .orElseThrow())
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisation(AuthenticatedUser user, UUID organisationId) {
        requireActiveMembership(user, organisationId);
        Organisation organisation = requireOrganisation(organisationId);
        return OrganisationResponse.from(organisation);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> listMembers(AuthenticatedUser user, UUID organisationId) {
        requireActiveMembership(user, organisationId);
        return membershipRepository.findByOrganisationIdAndStatus(organisationId, MembershipStatus.ACTIVE).stream()
                .map(MembershipResponse::from)
                .toList();
    }

    @Transactional
    public MembershipResponse inviteMember(AuthenticatedUser user, UUID organisationId, InviteMemberRequest request) {
        OrganisationMembership actorMembership = requireActiveMembership(user, organisationId);
        if (!actorMembership.getRole().canManageMembers()) {
            throw new AccessDeniedException("Insufficient permissions to invite members");
        }
        if (request.role() == OrganisationRole.OWNER) {
            throw new IllegalArgumentException("Cannot invite as OWNER; transfer ownership separately");
        }

        UserProfile inviter = userProfileService.requireProfileEntity(user.keycloakSub());
        UserProfile invitee = userProfileRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

        if (membershipRepository.existsByOrganisationIdAndUserIdAndStatus(
                organisationId, invitee.getId(), MembershipStatus.ACTIVE)) {
            throw new IllegalArgumentException("User is already an active member");
        }

        OrganisationMembership membership = new OrganisationMembership(
                organisationId,
                invitee.getId(),
                request.role(),
                inviter.getId()
        );
        membership = membershipRepository.save(membership);

        auditService.record(
                organisationId,
                inviter.getId(),
                user.keycloakSub(),
                "MEMBER_INVITED",
                "organisation_membership",
                membership.getId(),
                null,
                Map.of("userId", invitee.getId().toString(), "role", request.role().name())
        );

        return MembershipResponse.from(membership);
    }

    public OrganisationMembership requireActiveMembership(AuthenticatedUser user, UUID organisationId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        OrganisationMembership membership = membershipRepository
                .findByOrganisationIdAndUserId(organisationId, profile.getId())
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new TenantIsolationException(
                        "No active membership for organisation " + organisationId));

        if (user.hasRole("marketplace_admin")) {
            return membership;
        }
        return membership;
    }

    public void requireOrganisationRole(AuthenticatedUser user, UUID organisationId, OrganisationRole minimum) {
        OrganisationMembership membership = requireActiveMembership(user, organisationId);
        if (!hasMinimumRole(membership.getRole(), minimum)) {
            throw new AccessDeniedException("Required organisation role: " + minimum);
        }
    }

    private boolean hasMinimumRole(OrganisationRole actual, OrganisationRole required) {
        int actualRank = roleRank(actual);
        int requiredRank = roleRank(required);
        return actualRank <= requiredRank;
    }

    private int roleRank(OrganisationRole role) {
        return switch (role) {
            case OWNER -> 0;
            case ADMINISTRATOR -> 1;
            case PRODUCT_MANAGER, FINANCE_OFFICER -> 2;
            case SUPPORT_OFFICER, SECURITY_VIEWER -> 3;
        };
    }

    private Organisation requireOrganisation(UUID organisationId) {
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
    }
}
