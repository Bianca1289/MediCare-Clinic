package com.medicareclinic.backend.services;

import com.medicareclinic.backend.model.Receptionist;
import com.medicareclinic.backend.repository.ReceptionistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final ReceptionistRepository receptionistRepository;

    @Transactional
    public void createReceptionist(Receptionist receptionist) {
        receptionistRepository.save(receptionist);
    }

    @Transactional(readOnly = true)
    public Receptionist getReceptionistById(Long id) {
        return receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with id: " + id));
    }

    @Transactional
    public void updateReceptionist(Long id, Receptionist receptionistDetails) {
        Receptionist receptionist = getReceptionistById(id);

        receptionist.setNume(receptionistDetails.getNume());
        receptionist.setPrenume(receptionistDetails.getPrenume());
        receptionist.setEmail(receptionistDetails.getEmail());

        receptionistRepository.save(receptionist);
    }

    @Transactional
    public void deleteReceptionist(Long id) {
        Receptionist receptionist = getReceptionistById(id);
        receptionistRepository.delete(receptionist);
    }
}
