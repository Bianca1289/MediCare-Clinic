package com.medicareclinic.backend.services;

import com.medicareclinic.backend.model.Consultation;
import com.medicareclinic.backend.model.Doctor;
import com.medicareclinic.backend.model.Patient;
import com.medicareclinic.backend.model.Receptionist;
import com.medicareclinic.backend.repository.ConsultationRepository;
import com.medicareclinic.backend.repository.PatientRepository;
import com.medicareclinic.backend.repository.ReceptionistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ReceptionistService receptionistService;

    @Transactional
    public Consultation createConsultation(Consultation consultation, long receptionistId) {
        // Verificăm dacă pacientul există
        Patient patient = patientService.getPatientById(consultation.getPatient().getId());
        consultation.setPatient(patient);

        // Verify if the receptionist exists
        Receptionist receptionist = receptionistService.getReceptionistById(receptionistId);
        consultation.setReceptionist(receptionist);

        // Verificăm dacă medicul există
        Doctor doctor = doctorService.getDoctorById(consultation.getDoctor().getId());
        consultation.setDoctor(doctor);

        // Verificăm dacă medicul are deja o consultație la aceeași dată și oră
        List<Consultation> existingConsultations = consultationRepository
                .findByDoctorIdAndDataAndOra(doctor.getId(), consultation.getData(), consultation.getOra());

        if (!existingConsultations.isEmpty()) {
            throw new RuntimeException("Medicul are deja o consultație programată la această dată și oră");
        }

        return consultationRepository.save(consultation);
    }

    @Transactional(readOnly = true)
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Consultation getConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByPatientId(Long patientId) {
        patientService.getPatientById(patientId);
        return consultationRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByDoctorId(Long doctorId) {
        doctorService.getDoctorById(doctorId);
        return consultationRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Consultation> getConsultationsByStatus(String status) {
        return consultationRepository.findByStatus(status);
    }

    @Transactional
    public Consultation updateConsultation(Long id, Consultation consultationDetails) {
        Consultation consultation = getConsultationById(id);

        consultation.setData(consultationDetails.getData());
        consultation.setOra(consultationDetails.getOra());
        consultation.setMotiv(consultationDetails.getMotiv());
        consultation.setDiagnostic(consultationDetails.getDiagnostic());
        consultation.setSimptome(consultationDetails.getSimptome());
        consultation.setRecomandari(consultationDetails.getRecomandari());
        consultation.setStatus(consultationDetails.getStatus());

        return consultationRepository.save(consultation);
    }

    @Transactional
    public void deleteConsultation(Long id) {
        Consultation consultation = getConsultationById(id);
        consultationRepository.delete(consultation);
    }
}
