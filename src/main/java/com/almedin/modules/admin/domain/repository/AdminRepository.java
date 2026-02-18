package com.almedin.modules.admin.domain.repository;

import com.almedin.modules.admin.domain.model.Admin;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public interface AdminRepository {

    Optional<Admin> findByEmail(String email);
    void persist(Admin admin);
}