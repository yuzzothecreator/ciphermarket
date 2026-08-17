package com.ciphermarket.api.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationRoleTest {

    @Test
    void ownerCanManageMembers() {
        assertThat(OrganisationRole.OWNER.canManageMembers()).isTrue();
        assertThat(OrganisationRole.ADMINISTRATOR.canManageMembers()).isTrue();
        assertThat(OrganisationRole.PRODUCT_MANAGER.canManageMembers()).isFalse();
    }

    @Test
    void productManagerCanManageProducts() {
        assertThat(OrganisationRole.PRODUCT_MANAGER.canManageProducts()).isTrue();
        assertThat(OrganisationRole.SUPPORT_OFFICER.canManageProducts()).isFalse();
    }

    @Test
    void securityViewerCanViewSecurity() {
        assertThat(OrganisationRole.SECURITY_VIEWER.canViewSecurity()).isTrue();
        assertThat(OrganisationRole.FINANCE_OFFICER.canViewSecurity()).isFalse();
    }
}
