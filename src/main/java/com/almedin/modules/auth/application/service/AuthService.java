package com.almedin.modules.auth.application.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.almedin.modules.admin.domain.model.Admin;
import com.almedin.modules.admin.domain.repository.AdminRepository;
import com.almedin.modules.affiliates.domain.model.Affiliate;
import com.almedin.modules.affiliates.domain.repository.AffiliateRepository;
import com.almedin.modules.auth.application.dto.AuthRequest;
import com.almedin.modules.auth.application.dto.AuthResponse;
import com.almedin.modules.shared.domain.model.User;
import com.almedin.modules.specialists.domain.model.Specialist;
import com.almedin.modules.specialists.domain.repository.SpecialistRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    AffiliateRepository affiliateRepository;

    @Inject
    SpecialistRepository specialistRepository;

    @Inject
    AdminRepository adminRepository;

    public AuthResponse login(AuthRequest request) {
        String email = request.email();
        String password = request.password();

        Optional<? extends User> userOpt = affiliateRepository.findByEmail(email)
                .map(u -> (User) u)
                .or(() -> specialistRepository.findByEmail(email).map(u -> (User) u))
                .or(() -> adminRepository.findByEmail(email).map(u -> (User) u));

        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new IllegalStateException("La cuenta está desactivada");
        }

        if (!checkPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        Long id = resolveId(user);
        String token = generateToken(id, user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getRole().name(),
                user.getFirstName() + " " + user.getLastName(), id);
    }

    private Long resolveId(User user) {
        if (user instanceof Affiliate a) return a.getId();
        if (user instanceof Specialist s) return s.getId();
        if (user instanceof Admin adm) return adm.getId();
        throw new IllegalStateException("Tipo de usuario no reconocido");
    }

    private String generateToken(Long userId, String email, String role) {
        return Jwt.issuer("almedin-backend")
                .subject(email)
                .groups(role)
                .claim("userId", userId)
                .expiresIn(Duration.ofHours(8))
                .sign();
    }

    private boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword).verified;
    }
}