package com.ciphermarket.api.common.enums;

public enum PlatformRole {
    BUYER,
    CREATOR,
    MARKETPLACE_ADMIN,
    SECURITY_AUDITOR,
    SUPPORT_OFFICER;

    public static PlatformRole fromKeycloakRole(String role) {
        return switch (role.toLowerCase()) {
            case "buyer" -> BUYER;
            case "creator" -> CREATOR;
            case "marketplace_admin" -> MARKETPLACE_ADMIN;
            case "security_auditor" -> SECURITY_AUDITOR;
            case "support_officer" -> SUPPORT_OFFICER;
            default -> null;
        };
    }
}
