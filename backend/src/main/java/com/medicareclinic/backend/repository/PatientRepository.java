package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser_Username(String username);
}

