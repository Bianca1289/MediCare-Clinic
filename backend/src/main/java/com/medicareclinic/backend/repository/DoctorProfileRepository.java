package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUser_Username(String username);
    boolean existsByUser_Id(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);
    Page<DoctorProfile> findAll(Pageable pageable);
}
