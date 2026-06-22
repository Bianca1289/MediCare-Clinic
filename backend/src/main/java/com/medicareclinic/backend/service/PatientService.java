package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.CreatePatientRequest;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.User;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

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

        return patientRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Patient getPatientForUser(String username) {
        return patientRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient record not found for user: " + username));
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + id));
    }
}

