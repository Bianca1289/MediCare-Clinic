package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MedicalRecordRequest(
        @NotNull(message = "Patient ID is required")
        Long patientId,

        @Size(max = 10)
        String bloodType,

        @Size(max = 500)
        String allergies,

        @Size(max = 500)
        String chronicConditions,

        @Size(max = 2000)
        String notes,

        LocalDate dateOfBirth
) {}
