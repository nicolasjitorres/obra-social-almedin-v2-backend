package com.almedin.modules.auth.infrastructure.startup;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.admin.domain.model.Admin;
import com.almedin.modules.admin.domain.repository.AdminRepository;
import com.almedin.modules.shared.domain.enums.Role;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AdminInitializer {

    @Inject
    AdminRepository adminRepository;

    @ConfigProperty(name = "app.admin.email", defaultValue = "admin@almedin.com")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password", defaultValue = "Admin1234!")
    String adminPassword;

    @ConfigProperty(name = "app.admin.dni", defaultValue = "00000000")
    String adminDni;

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        if (adminRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Admin admin = Admin.builder()
                .firstName("Super")
                .lastName("Admin")
                .dni(adminDni)
                .email(adminEmail)
                .password(BCrypt.withDefaults().hashToString(12, adminPassword.toCharArray()))
                .role(Role.ADMIN)
                .active(true)
                .build();

        adminRepository.persist(admin);
    }
}