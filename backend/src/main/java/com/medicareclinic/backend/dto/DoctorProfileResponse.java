package com.medicareclinic.backend.dto;

import java.util.Set;

public record DoctorProfileResponse(
        Long id,
        String username,
        String licenseNumber,
        String bio,
        String phoneNumber,
        Set<String> specialties
) {}
