package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorProfile_Id(Long doctorProfileId);
    boolean existsByDoctorProfile_IdAndDayOfWeek(Long doctorProfileId, String dayOfWeek);
}
