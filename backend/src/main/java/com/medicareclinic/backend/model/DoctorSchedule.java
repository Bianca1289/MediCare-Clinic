package com.medicareclinic.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "ProgramDoctor")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medic_id", nullable = false)
    @NotNull(message = "Medicul este obligatoriu")
    private Doctor doctor;

    @NotBlank(message = "Ziua săptămânii este obligatorie")
    @Pattern(regexp = "LUNI|MARTI|MIERCURI|JOI|VINERI|SAMBATA|DUMINICA",
            message = "Zi invalidă. Valori permise: LUNI, MARTI, MIERCURI, JOI, VINERI, SAMBATA, DUMINICA")
    @Column(name = "zi_saptamana", nullable = false, length = 10)
    private String ziSaptamana;

    @NotNull(message = "Ora de început este obligatorie")
    @Column(name = "ora_inceput", nullable = false)
    private LocalTime oraInceput;

    @NotNull(message = "Ora de sfârșit este obligatorie")
    @Column(name = "ora_sfarsit", nullable = false)
    private LocalTime oraSfarsit;

    @AssertTrue(message = "Ora de sfârșit trebuie să fie după ora de început")
    public boolean isOraValida() {
        if (oraInceput == null || oraSfarsit == null) {
            return true;
        }
        return oraSfarsit.isAfter(oraInceput);
    }
}
