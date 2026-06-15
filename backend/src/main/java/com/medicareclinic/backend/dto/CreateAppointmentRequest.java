package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
        @NotNull(message = "patientId is required")
        Long patientId,

        @NotNull(message = "doctorUsername is required")
        String doctorUsername,

        @NotNull(message = "startTime is required (ISO-8601)")
        String startTime,

        @Size(max = 1000)
        String notes
) {}

