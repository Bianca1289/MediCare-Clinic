package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser_Username(String username);
    Page<Patient> findAll(Pageable pageable);
    Page<Patient> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
}
