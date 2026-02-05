package com.example.gestionimmobilier.entity.user;

import com.example.gestionimmobilier.entity.enums.StatutDossier;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Client extends Utilisateur {

    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    private StatutDossier statutDossier;
}
