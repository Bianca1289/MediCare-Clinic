package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @Size(max = 100)
        String contactInfo,

        @Email(message = "Valid email is required")
        String email
) {}
