package com.example.gestionimmobilier.models.entity.contrat;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "contrats")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contrat extends BaseEntity {

    @Column(unique = true)
    private String numContrat;

    private LocalDate dateSignature;
    private String documentUrl;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = true)
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "bien_id", nullable = false)
    private BienImmobilier bien;
}
