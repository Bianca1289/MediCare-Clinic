package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.DoctorProfileRequest;
import com.medicareclinic.backend.dto.DoctorProfileResponse;
import com.medicareclinic.backend.dto.UpdateDoctorProfileRequest;
import com.medicareclinic.backend.model.DoctorProfile;
import com.medicareclinic.backend.model.Specialty;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.DoctorProfileRepository;
import com.medicareclinic.backend.repository.SpecialtyRepository;
import com.medicareclinic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    @Transactional
    public DoctorProfileResponse create(DoctorProfileRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + request.username()));

        if (doctorProfileRepository.existsByUser_Id(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor profile already exists for user: " + request.username());
        }
        if (doctorProfileRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already registered: " + request.licenseNumber());
        }

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setLicenseNumber(request.licenseNumber());
        profile.setBio(request.bio());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setSpecialties(resolveSpecialties(request.specialtyIds()));

        DoctorProfile saved = doctorProfileRepository.save(profile);
        log.info("Created doctor profile for user: {}", request.username());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DoctorProfileResponse> getAll(Pageable pageable) {
        return doctorProfileRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getByUsername(String username) {
        DoctorProfile profile = doctorProfileRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found for: " + username));
        return toResponse(profile);
    }

    @Transactional
    public DoctorProfileResponse update(Long id, UpdateDoctorProfileRequest request) {
        DoctorProfile profile = findOrThrow(id);

        if (request.licenseNumber() != null && !request.licenseNumber().isBlank()) {
            if (!profile.getLicenseNumber().equals(request.licenseNumber())
                    && doctorProfileRepository.existsByLicenseNumber(request.licenseNumber())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already taken: " + request.licenseNumber());
            }
            profile.setLicenseNumber(request.licenseNumber());
        }
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.phoneNumber() != null) profile.setPhoneNumber(request.phoneNumber());
        if (request.specialtyIds() != null) profile.setSpecialties(resolveSpecialties(request.specialtyIds()));

        log.info("Updated doctor profile id={}", id);
        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional
    public void delete(Long id) {
        DoctorProfile profile = findOrThrow(id);
        doctorProfileRepository.delete(profile);
        log.info("Deleted doctor profile id={}", id);
    }

    private DoctorProfile findOrThrow(Long id) {
        return doctorProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found: " + id));
    }

    private Set<Specialty> resolveSpecialties(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return ids.stream()
                .map(sid -> specialtyRepository.findById(sid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialty not found: " + sid)))
                .collect(Collectors.toSet());
    }

    public DoctorProfileResponse toResponse(DoctorProfile p) {
        Set<String> specialtyNames = p.getSpecialties().stream()
                .map(Specialty::getName)
                .collect(Collectors.toSet());
        return new DoctorProfileResponse(
                p.getId(),
                p.getUser().getUsername(),
                p.getLicenseNumber(),
                p.getBio(),
                p.getPhoneNumber(),
                specialtyNames
        );
    }
}
