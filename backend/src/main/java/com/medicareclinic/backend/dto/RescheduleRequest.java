package com.medicareclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RescheduleRequest(@NotBlank String startTime) {}
