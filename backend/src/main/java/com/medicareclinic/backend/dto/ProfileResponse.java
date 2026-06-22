package com.medicareclinic.backend.dto;

import java.util.List;

public record ProfileResponse(
        Long id,
        String username,
        boolean enabled,
        List<String> roles
) {}

