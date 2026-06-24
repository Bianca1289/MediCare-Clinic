package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.CreatePatientRequest;
import com.medicareclinic.backend.dto.PatientResponse;
import com.medicareclinic.backend.dto.UpdatePatientRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional
    public Patient createPatient(CreatePatientRequest request) {
        Patient p = new Patient();
        p.setFullName(request.fullName());
        p.setContactInfo(request.contactInfo());
        p.setEmail(request.email());

        if (request.username() != null && !request.username().isBlank()) {
            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found: " + request.username()));
            p.setUser(user);
        }

        Patient saved = patientRepository.save(p);
        log.info("Created patient: {}", saved.getFullName());
        return saved;
    }

    /** Returns the patient record for the user, auto-creating a minimal record if none exists. */
    @Transactional
    public Patient getOrCreatePatientForUser(String username) {
        return patientRepository.findByUser_Username(username).orElseGet(() -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
            Patient p = new Patient();
            p.setUser(user);
            p.setFullName(username);
            Patient saved = patientRepository.save(p);
            log.info("Auto-created patient record for user: {}", username);
            return saved;
        });
    }

    @Transactional(readOnly = true)
    public Patient getPatientForUser(String username) {
        return patientRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient record not found for user: " + username));
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatientsPaged(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(String name, Pageable pageable) {
        return patientRepository.findByFullNameContainingIgnoreCase(name, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + id));
    }

    @Transactional
    public PatientResponse updatePatient(Long id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + id));

        patient.setFullName(request.fullName());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setCnp(request.cnp());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        Patient saved = patientRepository.save(patient);
        log.info("Updated patient id={}", id);
        return toResponse(saved);
    }

    @Transactional
    public PatientResponse updatePatientForUser(String username, UpdatePatientRequest request) {
        Patient patient = getOrCreatePatientForUser(username);

        patient.setFullName(request.fullName());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setCnp(request.cnp());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        Patient saved = patientRepository.save(patient);
        log.info("Self-updated patient record for user: {}", username);
        return toResponse(saved);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + id));
        patientRepository.delete(patient);
        log.info("Deleted patient id={}", id);
    }

    public PatientResponse toResponse(Patient p) {
        String username = p.getUser() != null ? p.getUser().getUsername() : null;
        return new PatientResponse(
                p.getId(),
                p.getFullName(),
                p.getPhone() != null ? p.getPhone() : p.getContactInfo(),
                p.getEmail(),
                username,
                p.getCnp(),
                p.getGender(),
                p.getAddress()
        );
    }
}
