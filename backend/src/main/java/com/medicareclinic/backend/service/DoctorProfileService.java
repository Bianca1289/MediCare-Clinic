package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.DoctorAvailabilityResponse;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        profile.setLocation(request.location());
        profile.setSpecialty(resolveSpecialty(request.specialtyId()));

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
        if (request.location() != null) profile.setLocation(request.location());
        if (request.specialtyId() != null) profile.setSpecialty(resolveSpecialty(request.specialtyId()));

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

    private Specialty resolveSpecialty(Long id) {
        if (id == null) return null;
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialty not found: " + id));
    }

    private static final Map<String, Integer> DAY_ORDER = Map.of(
            "MONDAY", 1, "TUESDAY", 2, "WEDNESDAY", 3, "THURSDAY", 4,
            "FRIDAY", 5, "SATURDAY", 6, "SUNDAY", 7
    );

    public DoctorProfileResponse toResponse(DoctorProfile p) {
        List<DoctorAvailabilityResponse> availability = p.getAvailability().stream()
                .map(a -> new DoctorAvailabilityResponse(
                        a.getDayOfWeek(),
                        a.getStartTime().toString(),
                        a.getEndTime().toString()))
                .sorted(Comparator.comparingInt(r -> DAY_ORDER.getOrDefault(r.dayOfWeek(), 99)))
                .collect(Collectors.toList());

        return new DoctorProfileResponse(
                p.getId(),
                p.getUser().getUsername(),
                p.getLicenseNumber(),
                p.getBio(),
                p.getPhoneNumber(),
                p.getLocation(),
                p.getAverageRating(),
                p.getSpecialty() != null ? p.getSpecialty().getName() : null,
                availability
        );
    }
}
