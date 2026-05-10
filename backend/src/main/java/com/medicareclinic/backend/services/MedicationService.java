package com.medicareclinic.backend.services;

import com.medicareclinic.backend.model.Medication;
import com.medicareclinic.backend.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {
    private final MedicationRepository medicationRepository;

    @Transactional
    public Medication createMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Transactional(readOnly = true)
    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Medication> searchMedications(String query) {
        if (query == null || query.trim().isEmpty()) {
            return medicationRepository.findAll();
        }
        return medicationRepository.searchMedicamente(query);
    }

    @Transactional(readOnly = true)
    public List<Medication> getMedicationsByNume(String nume) {
        return medicationRepository.findByNumeContainingIgnoreCase(nume);
    }

    @Transactional
    public Medication updateMedication(Long id, Medication medicationDetails) {
        Medication medication = getMedicationById(id);

        medication.setNume(medicationDetails.getNume());
        medication.setConcentratie(medicationDetails.getConcentratie());
        medication.setPret(medicationDetails.getPret());
        medication.setProducator(medicationDetails.getProducator());
        medication.setRestrictii(medicationDetails.getRestrictii());

        return medicationRepository.save(medication);
    }

    @Transactional
    public void deleteMedication(Long id) {
        Medication medication = getMedicationById(id);
        medicationRepository.delete(medication);
    }
}
