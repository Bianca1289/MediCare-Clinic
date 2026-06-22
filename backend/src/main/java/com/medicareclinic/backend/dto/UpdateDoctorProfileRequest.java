package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateDoctorProfileRequest(
        @Size(max = 50)
        String licenseNumber,

        @Size(max = 1000)
        String bio,

        @Size(max = 20)
        String phoneNumber,

        Set<Long> specialtyIds
) {}
