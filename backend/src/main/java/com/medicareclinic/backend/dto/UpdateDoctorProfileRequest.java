package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.Size;

public record UpdateDoctorProfileRequest(
        @Size(max = 50)
        String licenseNumber,

        @Size(max = 1000)
        String bio,

        @Size(max = 20)
        String phoneNumber,

        @Size(max = 100)
        String location,

        Long specialtyId
) {}
