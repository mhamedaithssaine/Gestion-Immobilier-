package com.example.gestionimmobilier.models.entity.agence;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.enums.StatutAgence;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "agences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Agence extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String email;

    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAgence statut;
}
