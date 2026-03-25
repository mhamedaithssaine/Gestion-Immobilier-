package com.example.gestionimmobilier.models.entity.contrat;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mandats_gestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MandatDeGestion extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String numMandat;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "bien_id", nullable = false)
    private BienImmobilier bien;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "commission_pct", precision = 5, scale = 2)
    private BigDecimal commissionPct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMandat statut;

    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @Column(name = "document_url")
    private String documentUrl;
}
