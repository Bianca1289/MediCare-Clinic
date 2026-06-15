package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.ProfileResponse;
import com.medicareclinic.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProfileService profileService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        List<ProfileResponse> users = profileService.getAllProfiles();
        return ResponseEntity.ok(Map.of(
                "message", "Admin Dashboard",
                "totalUsers", users.size(),
                "users", users
        ));
    }
}
