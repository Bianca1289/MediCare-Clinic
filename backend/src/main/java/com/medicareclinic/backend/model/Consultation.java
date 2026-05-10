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
import java.time.LocalTime;

@Entity
@Table(name = "Consult")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data consultației este obligatorie")
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @NotNull(message = "Ora consultației este obligatorie")
    @Column(name = "ora", nullable = false)
    private LocalTime ora;

    @NotBlank(message = "Motivul consultației este obligatoriu")
    @Size(max = 500, message = "Motivul nu poate depăși 500 de caractere")
    @Column(name = "motiv", nullable = false, length = 500)
    private String motiv;

    @Size(max = 1000, message = "Diagnosticul nu poate depăși 1000 de caractere")
    @Column(name = "diagnostic", length = 1000)
    private String diagnostic;

    @Size(max = 1000, message = "Simptomele nu pot depăși 1000 de caractere")
    @Column(name = "simptome", length = 1000)
    private String simptome;

    @Size(max = 1000, message = "Recomandările nu pot depăși 1000 de caractere")
    @Column(name = "recomandari", length = 1000)
    private String recomandari;

    @NotBlank(message = "Statusul este obligatoriu")
    @Pattern(regexp = "PROGRAMATA|IN_DESFASURARE|FINALIZATA|ANULATA",
            message = "Status invalid. Valori permise: PROGRAMATA, IN_DESFASURARE, FINALIZATA, ANULATA")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PROGRAMATA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacient_id", nullable = false)
    @NotNull(message = "Pacientul este obligatoriu")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctorul este obligatoriu")
    private Doctor doctor;

    @JsonIgnore
    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Prescription reteta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionist_id", nullable = false)
    @NotNull(message = "Receptionistul este obligatoriu")
    private Receptionist receptionist;
}
