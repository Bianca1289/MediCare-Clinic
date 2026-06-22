package com.medicareclinic.backend.dto;

import java.time.LocalDate;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String patientName,
        String bloodType,
        String allergies,
        String chronicConditions,
        String notes,
        LocalDate dateOfBirth
) {}
