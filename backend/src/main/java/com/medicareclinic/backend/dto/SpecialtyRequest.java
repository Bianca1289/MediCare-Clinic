package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtyRequest(
        @NotBlank(message = "Specialty name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {}
