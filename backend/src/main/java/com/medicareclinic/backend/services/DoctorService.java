package com.medicareclinic.backend.services;

import com.medicareclinic.backend.model.Doctor;
import com.medicareclinic.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional (readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional (readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    @Transactional (readOnly = true)
    public List<Doctor> getDoctorsBySpecializare(String specializare) {
        return doctorRepository.findBySpecializare(specializare);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);

        doctor.setNume(doctorDetails.getNume());
        doctor.setPrenume(doctorDetails.getPrenume());
        doctor.setSpecializare(doctorDetails.getSpecializare());
        doctor.setTelefon(doctorDetails.getTelefon());
        doctor.setEmail(doctorDetails.getEmail());

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}
