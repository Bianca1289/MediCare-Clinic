package com.medicareclinic.backend.repository;

import com.medicareclinic.backend.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByNumeContainingIgnoreCase(String nume);

    @Query("SELECT m FROM Medication m WHERE LOWER(m.nume) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Medication> searchMedicamente(@Param("query") String query);
}
