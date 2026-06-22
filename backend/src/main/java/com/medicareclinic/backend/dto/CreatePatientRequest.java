package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreatePatientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        String contactInfo,

        @Email(message = "Valid email is required")
        String email,

        String username
) {}

