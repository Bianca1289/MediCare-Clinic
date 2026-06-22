package com.medicareclinic.backend.dto;

public record PatientResponse(
        Long id,
        String fullName,
        String contactInfo,
        String email,
        String username
) {}

