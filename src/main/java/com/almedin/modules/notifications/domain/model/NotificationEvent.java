package com.almedin.modules.notifications.domain.model;

public record NotificationEvent(
        String type,
        Long specialistId,
        String message,
        String timestamp
) {}