package com.almedin.modules.shared.infrastructure.web;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
public class RateLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final int LOGIN_MAX_REQUESTS = 5;
    private static final int LOGIN_WINDOW_SECONDS = 60;
    private static final int GENERAL_MAX_REQUESTS = 100;
    private static final int GENERAL_WINDOW_SECONDS = 60;

    private static final String RATE_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_REMAINING_HEADER = "X-RateLimit-Remaining";

    private final Map<String, RequestCounter> loginCounters = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> generalCounters = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext ctx) {
        String ip = extractIp(ctx);
        String path = ctx.getUriInfo().getPath();
        boolean isLogin = path.contains("auth/login");

        int maxRequests = isLogin ? LOGIN_MAX_REQUESTS : GENERAL_MAX_REQUESTS;
        int windowSeconds = isLogin ? LOGIN_WINDOW_SECONDS : GENERAL_WINDOW_SECONDS;
        Map<String, RequestCounter> counters = isLogin ? loginCounters : generalCounters;

        long now = Instant.now().getEpochSecond();
        RequestCounter counter = counters.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart >= windowSeconds) {
                return new RequestCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        ctx.setProperty(RATE_LIMIT_HEADER, maxRequests);
        ctx.setProperty(RATE_REMAINING_HEADER, Math.max(0, maxRequests - counter.count));

        if (counter.count > maxRequests) {
            long retryAfter = windowSeconds - (now - counter.windowStart);
            ctx.abortWith(Response.status(429)
                    .header("Retry-After", retryAfter)
                    .header(RATE_LIMIT_HEADER, maxRequests)
                    .header(RATE_REMAINING_HEADER, 0)
                    .entity("{\"status\":429,\"error\":\"Too Many Requests\"," +
                            "\"message\":\"Demasiadas solicitudes. Intente en " + retryAfter + " segundos.\"}")
                    .type("application/json")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) {
        Object limit = requestCtx.getProperty(RATE_LIMIT_HEADER);
        Object remaining = requestCtx.getProperty(RATE_REMAINING_HEADER);
        if (limit != null) {
            responseCtx.getHeaders().add(RATE_LIMIT_HEADER, limit);
            responseCtx.getHeaders().add(RATE_REMAINING_HEADER, remaining);
        }
    }

    private String extractIp(ContainerRequestContext ctx) {
        String forwarded = ctx.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = ctx.getHeaderString("X-Real-IP");
        return realIp != null ? realIp : "unknown";
    }

    private static class RequestCounter {
        long windowStart;
        int count;

        RequestCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}