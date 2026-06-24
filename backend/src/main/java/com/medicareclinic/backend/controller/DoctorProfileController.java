package com.medicareclinic.backend.controller;

import com.medicareclinic.backend.dto.DoctorProfileRequest;
import com.medicareclinic.backend.dto.DoctorProfileResponse;
import com.medicareclinic.backend.dto.UpdateDoctorProfileRequest;
import com.medicareclinic.backend.service.DoctorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors/profiles")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DoctorProfileResponse>> getAll(
            @PageableDefault(size = 10, sort = "user.username") Pageable pageable) {
        return ResponseEntity.ok(doctorProfileService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DoctorProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorProfileService.getById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DoctorProfileResponse> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(doctorProfileService.getByUsername(username));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(doctorProfileService.getByUsername(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorProfileResponse> create(@Valid @RequestBody DoctorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorProfileService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorProfileResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorProfileRequest request) {
        return ResponseEntity.ok(doctorProfileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
