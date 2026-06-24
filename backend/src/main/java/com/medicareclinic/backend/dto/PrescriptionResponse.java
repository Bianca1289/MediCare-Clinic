package com.medicareclinic.backend.dto;

public record PrescriptionResponse(
        Long id,
        String patientName,
        String doctorUsername,
        Long appointmentId,
        String medicationName,
        String dosage,
        String frequency,
        String duration,
        String instructions,
        String issuedAt,
        String validUntil
) {}
