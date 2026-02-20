package com.almedin.modules.shared.infrastructure.web;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        res.getHeaders().putSingle("X-Content-Type-Options",  "nosniff");
        res.getHeaders().putSingle("X-Frame-Options",         "DENY");
        res.getHeaders().putSingle("X-XSS-Protection",        "1; mode=block");
        res.getHeaders().putSingle("Referrer-Policy",         "strict-origin-when-cross-origin");
        res.getHeaders().putSingle("Permissions-Policy",      "geolocation=(), microphone=(), camera=()");
        res.getHeaders().putSingle(
                "Content-Security-Policy",
                "default-src 'self'; frame-ancestors 'none'"
        );
    }
}