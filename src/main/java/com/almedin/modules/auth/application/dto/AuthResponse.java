package com.almedin.modules.auth.application.dto;

public record AuthResponse(
        String token,
        String role,
        String fullName,
        Long userId
) {}