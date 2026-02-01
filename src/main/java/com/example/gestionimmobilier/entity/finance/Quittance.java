package com.example.gestionimmobilier.entity.finance;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.user.Proprietaire;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Quittance extends BaseEntity {

    @Column(unique = true)
    private String referenceQuittance;

    private int mois;
    private int annee;

    private String urlPdf;
    private LocalDateTime dateGeneration;

    @OneToOne
    private Versement versement;

    @ManyToOne
    private Proprietaire proprietaire;
}
