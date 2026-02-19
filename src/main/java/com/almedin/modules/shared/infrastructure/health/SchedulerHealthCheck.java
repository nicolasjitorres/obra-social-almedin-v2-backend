package com.almedin.modules.shared.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class SchedulerHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Reminder Scheduler")
                .up()
                .withData("cron", "0 0 8 * * ?")
                .withData("horasBefore", 24)
                .build();
    }
}