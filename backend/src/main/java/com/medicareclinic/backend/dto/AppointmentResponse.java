package com.medicareclinic.backend.dto;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        String doctorUsername,
        LocalDateTime startTime,
        String status,
        String notes
) {}

