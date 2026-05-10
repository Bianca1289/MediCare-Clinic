package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCnp(String cnp);

    Optional<Patient> findByEmail(String email);

    boolean existsByCnp(String cnp);

    boolean existsByEmail(String email);
}
