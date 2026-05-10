package com.medicareclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Reteta")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data emiterii este obligatorie")
    @Column(name = "data_emitere", nullable = false)
    private LocalDate dataEmitere;

    @Size(max = 500, message = "Observațiile nu pot depăși 500 de caractere")
    @Column(name = "observatii", length = 500)
    private String observatii;

    @NotBlank(message = "Statusul este obligatoriu")
    @Pattern(regexp = "ACTIVA|ELIBERATA|EXPIRATA",
            message = "Status invalid. Valori permise: ACTIVA, ELIBERATA, EXPIRATA")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacient_id", nullable = false)
    @NotNull(message = "Pacientul este obligatoriu")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctorul este obligatoriu")
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultatie_id")
    private Consultation consultation;

    @JsonIgnore
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionMedication> medicamente = new ArrayList<>();
}
