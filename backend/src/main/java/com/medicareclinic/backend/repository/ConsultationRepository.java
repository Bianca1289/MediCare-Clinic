package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByPatientId(Long patientId);

    List<Consultation> findByDoctorId(Long doctorId);

    List<Consultation> findByStatus(String status);

    List<Consultation> findByDoctorIdAndData(Long doctorId, LocalDate data);

    @Query("SELECT c FROM Consultation c WHERE c.doctor.id = :doctorId AND c.data = :data AND c.ora = :ora")
    List<Consultation> findByDoctorIdAndDataAndOra(@Param("doctorId") Long doctorId,
                                                   @Param("data") LocalDate data,
                                                   @Param("ora") LocalTime ora);
}
