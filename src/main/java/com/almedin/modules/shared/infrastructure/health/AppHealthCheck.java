package com.almedin.modules.shared.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class AppHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Almedin Backend")
                .up()
                .withData("version", "1.0.0")
                .withData("profile", System.getProperty("quarkus.profile", "dev"))
                .build();
    }
}