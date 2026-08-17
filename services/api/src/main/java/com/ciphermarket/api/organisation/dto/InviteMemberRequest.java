package com.ciphermarket.api.organisation.dto;

import com.ciphermarket.api.common.enums.OrganisationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @Email @NotNull String email,
        @NotNull OrganisationRole role
) {
}
