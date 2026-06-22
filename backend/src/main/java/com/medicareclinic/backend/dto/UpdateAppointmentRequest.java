package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentRequest(
        @Size(max = 50)
        String status,

        @Size(max = 1000)
        String notes,

        String startTime
) {}
