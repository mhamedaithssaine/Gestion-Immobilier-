package com.example.gestionimmobilier.models.entity.contrat;

import jakarta.persistence.Entity;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "baux")
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
