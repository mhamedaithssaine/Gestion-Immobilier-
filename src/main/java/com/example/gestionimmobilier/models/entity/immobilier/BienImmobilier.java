package com.example.gestionimmobilier.models.entity.immobilier;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "biens_immobiliers")
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
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;
}
