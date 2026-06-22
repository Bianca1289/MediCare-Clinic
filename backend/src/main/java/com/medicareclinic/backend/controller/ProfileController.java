package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.ProfileResponse;
import com.medicareclinic.backend.dto.UpdateProfileRequest;
import com.medicareclinic.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getCurrentProfile(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateCurrentProfile(authentication, request));
    }

    @PostMapping("/me/password")
    @Transactional
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody com.medicareclinic.backend.dto.ChangePasswordRequest request) {
        profileService.changePassword(authentication, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}

