package com.almedin.modules.admin.infrastructure.persistence;

import com.almedin.modules.admin.domain.model.Admin;
import com.almedin.modules.admin.domain.repository.AdminRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Optional;

@ApplicationScoped
public class EntityManagerAdminRepository implements AdminRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Admin> findByEmail(String email) {
        return em.createQuery(
                        "SELECT a FROM Admin a WHERE a.email = :email AND a.active = true", Admin.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void persist(Admin admin) {
        em.persist(admin);
    }
}