package com.medicareclinic.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RetetaMedicament")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PrescriptionMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reteta_id", nullable = false)
    @NotNull(message = "Rețeta este obligatorie")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    @NotNull(message = "Medicamentul este obligatoriu")
    private Medication medication;

    @NotBlank(message = "Dozajul este obligatoriu")
    @Size(max = 100, message = "Dozajul nu poate depăși 100 de caractere")
    @Column(name = "dozaj", nullable = false, length = 100)
    private String dozaj;

    @NotBlank(message = "Frecvența este obligatorie")
    @Size(max = 100, message = "Frecvența nu poate depăși 100 de caractere")
    @Column(name = "frecventa", nullable = false, length = 100)
    private String frecventa;

    @NotNull(message = "Durata în zile este obligatorie")
    @Min(value = 1, message = "Durata trebuie să fie cel puțin 1 zi")
    @Max(value = 365, message = "Durata nu poate depăși 365 de zile")
    @Column(name = "durata_zile", nullable = false)
    private Integer durataZile;

    @Size(max = 300, message = "Instrucțiunile nu pot depăși 300 de caractere")
    @Column(name = "instructiuni", length = 300)
    private String instructiuni;
}
