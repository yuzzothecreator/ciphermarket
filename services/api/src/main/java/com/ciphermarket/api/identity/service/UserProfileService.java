package com.ciphermarket.api.identity.service;

import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.dto.UpdateUserProfileRequest;
import com.ciphermarket.api.identity.dto.UserProfileResponse;
import com.ciphermarket.api.identity.repository.UserProfileRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponse getOrCreateProfile(AuthenticatedUser user) {
        UserProfile profile = userProfileRepository.findByKeycloakSub(user.keycloakSub())
                .orElseGet(() -> createProfile(user));
        return UserProfileResponse.from(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(AuthenticatedUser user, UpdateUserProfileRequest request) {
        UserProfile profile = requireProfile(user.keycloakSub());
        profile.updateProfile(request.displayName(), request.locale(), request.timezone());
        return UserProfileResponse.from(userProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfile requireProfileEntity(String keycloakSub) {
        return requireProfile(keycloakSub);
    }

    private UserProfile createProfile(AuthenticatedUser user) {
        UserProfile profile = new UserProfile(
                user.keycloakSub(),
                user.email(),
                user.displayName() != null ? user.displayName() : user.email()
        );
        return userProfileRepository.save(profile);
    }

    private UserProfile requireProfile(String keycloakSub) {
        return userProfileRepository.findByKeycloakSub(keycloakSub)
                .orElseThrow(() -> new IllegalStateException("User profile not provisioned"));
    }
}
