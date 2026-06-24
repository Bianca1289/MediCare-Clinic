package com.medicareclinic.backend.dto;

public record PatientResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        String username,
        String cnp,
        String gender,
        String address
) {}
