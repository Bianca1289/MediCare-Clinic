package com.medicareclinic.backend.service;

import com.medicareclinic.backend.dto.SpecialtyRequest;
import com.medicareclinic.backend.dto.SpecialtyResponse;
import com.medicareclinic.backend.model.Specialty;
import com.medicareclinic.backend.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        if (specialtyRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Specialty already exists: " + request.name());
        }
        Specialty specialty = new Specialty(request.name(), request.description());
        Specialty saved = specialtyRepository.save(specialty);
        log.info("Created specialty: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAll() {
        return specialtyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public SpecialtyResponse update(Long id, SpecialtyRequest request) {
        Specialty specialty = findOrThrow(id);
        if (!specialty.getName().equals(request.name()) && specialtyRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Specialty name already taken: " + request.name());
        }
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        log.info("Updated specialty id={}", id);
        return toResponse(specialtyRepository.save(specialty));
    }

    @Transactional
    public void delete(Long id) {
        Specialty specialty = findOrThrow(id);
        specialtyRepository.delete(specialty);
        log.info("Deleted specialty id={}", id);
    }

    private Specialty findOrThrow(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specialty not found: " + id));
    }

    public SpecialtyResponse toResponse(Specialty s) {
        return new SpecialtyResponse(s.getId(), s.getName(), s.getDescription());
    }
}
