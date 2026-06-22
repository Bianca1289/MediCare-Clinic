package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DoctorProfileRequest(
        @NotBlank(message = "Username of the doctor user is required")
        String username,

        @NotBlank(message = "License number is required")
        @Size(max = 50)
        String licenseNumber,

        @Size(max = 1000)
        String bio,

        @Size(max = 20)
        String phoneNumber,

        Set<Long> specialtyIds
) {}
