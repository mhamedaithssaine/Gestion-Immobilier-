package com.example.gestionimmobilier.models.entity.user;

import com.example.gestionimmobilier.models.entity.agence.Agence;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Agent extends Utilisateur {

    private String matricule;
    private String agenceNom;

    @ManyToOne(fetch = FetchType.LAZY)
    private Agence agence;
}