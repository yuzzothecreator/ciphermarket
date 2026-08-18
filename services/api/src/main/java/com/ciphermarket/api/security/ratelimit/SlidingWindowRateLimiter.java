package com.ciphermarket.api.security.ratelimit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window limiter keyed by client identity. Suitable for a single API instance.
 * Multi-instance production deployments should terminate rate limits at the gateway
 * or back this with Redis.
 */
public final class SlidingWindowRateLimiter {

    private final int limit;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int limit, long windowMillis) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        if (windowMillis < 1) {
            throw new IllegalArgumentException("windowMillis must be at least 1");
        }
        this.limit = limit;
        this.windowMillis = windowMillis;
    }

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(key, ignored -> new Window(now));
        synchronized (window) {
            if (now - window.windowStartMs >= windowMillis) {
                window.windowStartMs = now;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= limit;
        }
    }

    public void evictExpired(long now) {
        Iterator<Map.Entry<String, Window>> iterator = windows.entrySet().iterator();
        while (iterator.hasNext()) {
            Window window = iterator.next().getValue();
            synchronized (window) {
                if (now - window.windowStartMs >= windowMillis * 2) {
                    iterator.remove();
                }
            }
        }
    }

    int trackedKeyCount() {
        return windows.size();
    }

    private static final class Window {
        private final AtomicInteger count = new AtomicInteger();
        private long windowStartMs;

        private Window(long windowStartMs) {
            this.windowStartMs = windowStartMs;
        }
    }
}
