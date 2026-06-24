package com.medicareclinic.backend.dto;

import java.util.List;

public record DoctorProfileResponse(
        Long id,
        String username,
        String licenseNumber,
        String bio,
        String phoneNumber,
        String location,
        Double averageRating,
        String specialty,
        List<DoctorAvailabilityResponse> availability
) {}
