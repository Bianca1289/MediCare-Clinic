package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient_User_UsernameOrderByIssuedAtDesc(String username);
    boolean existsByAppointment_Id(Long appointmentId);
}
