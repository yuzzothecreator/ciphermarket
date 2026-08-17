package com.ciphermarket.api.identity.dto;

import com.ciphermarket.api.common.enums.UserStatus;
import com.ciphermarket.api.identity.domain.UserProfile;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        String avatarUrl,
        String locale,
        String timezone,
        UserStatus status,
        boolean mfaEnabled,
        Instant createdAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getLocale(),
                profile.getTimezone(),
                profile.getStatus(),
                profile.isMfaEnabled(),
                profile.getCreatedAt()
        );
    }
}
