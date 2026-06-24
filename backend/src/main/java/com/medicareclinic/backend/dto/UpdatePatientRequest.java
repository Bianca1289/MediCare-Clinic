package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @Size(max = 20)
        String phone,

        @Email(message = "Valid email is required")
        String email,

        @Size(max = 13)
        String cnp,

        @Size(max = 10)
        String gender,

        @Size(max = 255)
        String address
) {}
