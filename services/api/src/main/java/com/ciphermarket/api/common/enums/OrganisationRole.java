package com.ciphermarket.api.common.enums;

public enum OrganisationRole {
    OWNER,
    ADMINISTRATOR,
    PRODUCT_MANAGER,
    FINANCE_OFFICER,
    SUPPORT_OFFICER,
    SECURITY_VIEWER;

    public boolean canManageMembers() {
        return this == OWNER || this == ADMINISTRATOR;
    }

    public boolean canManageProducts() {
        return this == OWNER || this == ADMINISTRATOR || this == PRODUCT_MANAGER;
    }

    public boolean canViewSecurity() {
        return this == OWNER || this == ADMINISTRATOR || this == SECURITY_VIEWER;
    }
}
