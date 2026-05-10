package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecializare(String specializare);

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);
}
