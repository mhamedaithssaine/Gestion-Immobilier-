package com.example.gestionimmobilier.models.entity.user;

import com.example.gestionimmobilier.models.enums.StatutDossier;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;

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
