package com.ciphermarket.api.organisation.domain;

import com.ciphermarket.api.common.enums.MembershipStatus;
import com.ciphermarket.api.common.enums.OrganisationRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisation_memberships")
public class OrganisationMembership {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganisationRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected OrganisationMembership() {
    }

    public OrganisationMembership(
            UUID organisationId,
            UUID userId,
            OrganisationRole role,
            UUID invitedByUserId
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.userId = userId;
        this.role = role;
        this.invitedByUserId = invitedByUserId;
        this.joinedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (joinedAt == null) {
            joinedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrganisationRole getRole() {
        return role;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public UUID getInvitedByUserId() {
        return invitedByUserId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void changeRole(OrganisationRole role) {
        this.role = role;
    }

    public void suspend() {
        this.status = MembershipStatus.SUSPENDED;
    }

    public void remove() {
        this.status = MembershipStatus.REMOVED;
    }
}
