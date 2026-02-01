package com.example.gestionimmobilier.entity.immobilier;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.enums.StatutBien;
import com.example.gestionimmobilier.entity.user.Proprietaire;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BienImmobilier extends BaseEntity {

    @Column(unique = true)
    private String reference;

    private String titre;
    private Double surface;
    private BigDecimal prixBase;

    @Enumerated(EnumType.STRING)
    private StatutBien statut;

    @ElementCollection
    @CollectionTable(
            name = "bien_images",
            joinColumns = @JoinColumn(name = "bien_id")
    )
    private List<String> images;

    @OneToOne(cascade = CascadeType.ALL)
    private Adresse adresse;

    @ManyToOne
    private Proprietaire proprietaire;
}
