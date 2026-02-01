package com.example.gestionimmobilier.entity.contrat;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.entity.user.Client;
import com.example.gestionimmobilier.entity.user.Proprietaire;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
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
    private Client client;

    @ManyToOne
    private Proprietaire proprietaire;

    @OneToOne
    private BienImmobilier bien;
}
