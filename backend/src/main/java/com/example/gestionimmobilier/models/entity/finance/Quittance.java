package com.example.gestionimmobilier.models.entity.finance;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "quittances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Quittance extends BaseEntity {

    @Column(name = "reference_quittance", unique = true, nullable = false)
    private String referenceQuittance;

    @Column(nullable = false)
    private int mois;

    @Column(nullable = false)
    private int annee;

    @Column(name = "url_pdf")
    private String urlPdf;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @OneToOne
    @JoinColumn(name = "versement_id", nullable = false, unique = true)
    private Versement versement;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;
}
