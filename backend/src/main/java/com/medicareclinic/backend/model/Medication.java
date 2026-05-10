package com.medicareclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Medicament")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele medicamentului este obligatoriu")
    @Size(min = 2, max = 100, message = "Numele trebuie să aibă între 2 și 100 de caractere")
    @Column(name = "nume", nullable = false, length = 100)
    private String nume;

    @NotBlank(message = "Concentrația este obligatorie")
    @Size(max = 50, message = "Concentrația nu poate depăși 50 de caractere")
    @Column(name = "concentratie", nullable = false, length = 50)
    private String concentratie;

    @NotNull(message = "Prețul este obligatoriu")
    @DecimalMin(value = "0.0", inclusive = false, message = "Prețul trebuie să fie pozitiv")
    @Column(name = "pret", nullable = false, precision = 10, scale = 2)
    private BigDecimal pret;

    @NotBlank(message = "Producătorul este obligatoriu")
    @Size(max = 100, message = "Producătorul nu poate depăși 100 de caractere")
    @Column(name = "producator", nullable = false, length = 100)
    private String producator;

    @Size(max = 500, message = "Restricțiile nu pot depăși 500 de caractere")
    @Column(name = "restrictii", length = 500)
    private String restrictii;

    @JsonIgnore
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionMedication> prescriptions = new ArrayList<>();
}
