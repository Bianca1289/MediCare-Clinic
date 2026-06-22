package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.MedicalRecordRequest;
import com.medicareclinic.backend.dto.MedicalRecordResponse;
import com.medicareclinic.backend.dto.UpdateMedicalRecordRequest;
import com.medicareclinic.backend.model.MedicalRecord;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.repository.MedicalRecordRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + request.patientId()));

        if (medicalRecordRepository.existsByPatient_Id(request.patientId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medical record already exists for patient: " + request.patientId());
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setBloodType(request.bloodType());
        record.setAllergies(request.allergies());
        record.setChronicConditions(request.chronicConditions());
        record.setNotes(request.notes());
        record.setDateOfBirth(request.dateOfBirth());

        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("Created medical record for patient id={}", request.patientId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getAll(Pageable pageable) {
        return medicalRecordRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getByPatientId(Long patientId) {
        MedicalRecord record = medicalRecordRepository.findByPatient_Id(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No medical record for patient: " + patientId));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getForCurrentPatient(String username) {
        MedicalRecord record = medicalRecordRepository.findByPatient_User_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No medical record found for your account"));
        return toResponse(record);
    }

    @Transactional
    public MedicalRecordResponse update(Long id, UpdateMedicalRecordRequest request) {
        MedicalRecord record = findOrThrow(id);

        if (request.bloodType() != null) record.setBloodType(request.bloodType());
        if (request.allergies() != null) record.setAllergies(request.allergies());
        if (request.chronicConditions() != null) record.setChronicConditions(request.chronicConditions());
        if (request.notes() != null) record.setNotes(request.notes());
        if (request.dateOfBirth() != null) record.setDateOfBirth(request.dateOfBirth());

        log.info("Updated medical record id={}", id);
        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        MedicalRecord record = findOrThrow(id);
        medicalRecordRepository.delete(record);
        log.info("Deleted medical record id={}", id);
    }

    private MedicalRecord findOrThrow(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found: " + id));
    }

    public MedicalRecordResponse toResponse(MedicalRecord r) {
        return new MedicalRecordResponse(
                r.getId(),
                r.getPatient().getId(),
                r.getPatient().getFullName(),
                r.getBloodType(),
                r.getAllergies(),
                r.getChronicConditions(),
                r.getNotes(),
                r.getDateOfBirth()
        );
    }
}
