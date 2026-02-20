package com.almedin.modules.notifications.application.service;

import com.almedin.modules.notifications.domain.model.NotificationEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class NotificationService {

    private final Map<Long, BroadcastProcessor<NotificationEvent>> channels =
            new ConcurrentHashMap<>();

    public Multi<NotificationEvent> streamFor(Long specialistId) {
        return getOrCreate(specialistId).toHotStream();
    }

    public void notify(Long specialistId, NotificationEvent event) {
        BroadcastProcessor<NotificationEvent> processor = channels.get(specialistId);
        if (processor != null) {
            processor.onNext(event);
        }
    }

    private BroadcastProcessor<NotificationEvent> getOrCreate(Long specialistId) {
        return channels.computeIfAbsent(specialistId, id -> BroadcastProcessor.create());
    }
}