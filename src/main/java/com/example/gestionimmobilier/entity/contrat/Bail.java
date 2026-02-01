package com.example.gestionimmobilier.entity.contrat;

import jakarta.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Bail extends Contrat {

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private BigDecimal loyerHC;
    private BigDecimal charges;
}
