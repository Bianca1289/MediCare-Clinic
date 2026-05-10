package com.medicareclinic.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medicareclinic.backend.model.Receptionist;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {

    boolean existsByEmail(String email);

    Optional<Receptionist> findByEmail(String email);
}
