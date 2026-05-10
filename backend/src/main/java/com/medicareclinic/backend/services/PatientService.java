package com.medicareclinic.backend.services;

import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public void createPatient(Patient patient) {
        patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
         return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    @Transactional
    public void updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        patient.setNume(patientDetails.getNume());
        patient.setPrenume(patientDetails.getPrenume());
        patient.setCnp(patientDetails.getCnp());
        patient.setDataNasterii(patientDetails.getDataNasterii());
        patient.setTelefon(patientDetails.getTelefon());
        patient.setEmail(patientDetails.getEmail());
        patient.setAdresa(patientDetails.getAdresa());
        patient.setAlergii(patientDetails.getAlergii());

        patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
}
