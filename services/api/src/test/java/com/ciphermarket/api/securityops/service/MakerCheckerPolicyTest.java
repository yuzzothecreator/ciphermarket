package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.common.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class MakerCheckerPolicyTest {

    @Test
    void rejectsSameActor() {
        UUID actor = UUID.randomUUID();
        assertThatThrownBy(() -> MakerCheckerPolicy.assertDistinctActors(actor, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Maker cannot check");
    }

    @Test
    void allowsDistinctActors() {
        assertThatCode(() -> MakerCheckerPolicy.assertDistinctActors(UUID.randomUUID(), UUID.randomUUID()))
                .doesNotThrowAnyException();
    }
}
