package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.PrescriptionResponse;
import com.medicareclinic.backend.model.Prescription;
import com.medicareclinic.backend.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getForPatient(String username) {
        return prescriptionRepository
                .findByPatient_User_UsernameOrderByIssuedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PrescriptionResponse toResponse(Prescription p) {
        return new PrescriptionResponse(
                p.getId(),
                p.getPatient().getFullName(),
                p.getDoctor().getUsername(),
                p.getAppointment() != null ? p.getAppointment().getId() : null,
                p.getMedicationName(),
                p.getDosage(),
                p.getFrequency(),
                p.getDuration(),
                p.getInstructions(),
                p.getIssuedAt().toString(),
                p.getValidUntil() != null ? p.getValidUntil().toString() : null
        );
    }
}
