package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByPatient_Id(Long patientId);
    Optional<MedicalRecord> findByPatient_User_Username(String username);
    boolean existsByPatient_Id(Long patientId);
    Page<MedicalRecord> findAll(Pageable pageable);
}
