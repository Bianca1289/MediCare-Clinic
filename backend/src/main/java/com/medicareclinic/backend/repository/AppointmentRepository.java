package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor_Username(String username);
    List<Appointment> findByDoctor_Id(Long doctorId);
    List<Appointment> findByPatient_Id(Long patientId);
    List<Appointment> findByPatient_User_Username(String username);
    Page<Appointment> findByDoctor_Username(String username, Pageable pageable);
    Page<Appointment> findByPatient_User_Username(String username, Pageable pageable);
    Page<Appointment> findAll(Pageable pageable);
}
