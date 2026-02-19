package com.almedin.modules.specialists.domain.repository;

import com.almedin.modules.specialists.domain.model.Specialist;

import java.util.List;
import java.util.Optional;

public interface SpecialistRepository {
    List<Specialist> listAll();
    Optional<Specialist> findById(Long id);
    Optional<Specialist> findByDni(String dni);
    Optional<Specialist> findByEmail(String email);
    void persist(Specialist specialist);
    void deactivate(Specialist specialist);
}