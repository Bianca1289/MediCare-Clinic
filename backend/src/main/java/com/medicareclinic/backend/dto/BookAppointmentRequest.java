package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record BookAppointmentRequest(
        @NotBlank(message = "Doctor username is required")
        String doctorUsername,

        @NotBlank(message = "Start time is required")
        String startTime,

        String notes
) {}

