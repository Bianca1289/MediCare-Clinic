package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.ProfileResponse;
import com.medicareclinic.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final ProfileService profileService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome, " + authentication.getName(),
                "username", authentication.getName()
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfileResponse>> allUsers() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }
}
