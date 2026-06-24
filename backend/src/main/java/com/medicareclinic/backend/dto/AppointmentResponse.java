package com.medicareclinic.backend.dto;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        String doctorUsername,
        String startTime,
        String status,
        String notes
) {}
