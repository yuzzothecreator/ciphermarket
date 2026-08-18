package com.ciphermarket.api.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlidingWindowRateLimiterTest {

    @Test
    void allowsUpToLimitThenRejects() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, 60_000L);

        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isFalse();
    }

    @Test
    void isolatesKeys() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, 60_000L);

        assertThat(limiter.tryAcquire("a")).isTrue();
        assertThat(limiter.tryAcquire("b")).isTrue();
        assertThat(limiter.tryAcquire("a")).isFalse();
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThatThrownBy(() -> new SlidingWindowRateLimiter(0, 1000L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
