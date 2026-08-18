package com.ciphermarket.api.security.ratelimit;

import com.ciphermarket.api.common.web.CorrelationIdFilter;
import com.ciphermarket.api.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final SlidingWindowRateLimiter limiter;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
        int limit = Math.max(properties.requestsPerMinute(), 1);
        this.limiter = new SlidingWindowRateLimiter(limit, 60_000L);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.enabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = clientKey(request);
        if (!limiter.tryAcquire(key)) {
            String correlationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = """
                    {"type":"about:blank","title":"Too Many Requests","status":429,\
                    "detail":"Rate limit exceeded. Retry after 60 seconds.","correlationId":"%s"}
                    """.formatted(correlationId == null ? "" : correlationId);
            response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedDelay = 300_000)
    void evictExpiredWindows() {
        limiter.evictExpired(System.currentTimeMillis());
    }

    private String clientKey(HttpServletRequest request) {
        if (properties.trustForwardedFor()) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }
}
