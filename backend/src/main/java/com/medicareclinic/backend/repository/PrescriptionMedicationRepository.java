package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.PrescriptionMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicationRepository extends JpaRepository<PrescriptionMedication, Long> {
    List<PrescriptionMedication> findByPrescriptionId(Long prescriptionId);

    List<PrescriptionMedication> findByMedicationId(Long medicationId);
}
