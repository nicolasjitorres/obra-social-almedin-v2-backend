package com.almedin.modules.specialists.domain.repository;

import com.almedin.modules.specialists.domain.model.Specialist;

import java.util.List;
import java.util.Optional;

public interface SpecialistRepository {

    List<Specialist> listAllSpecialists();
    Optional<Specialist> findSpecialistById(Long id);
    Optional<Specialist> findSpecialistByDni(String dni);
    void persist(Specialist specialist);
    void delete(Specialist specialist);
}