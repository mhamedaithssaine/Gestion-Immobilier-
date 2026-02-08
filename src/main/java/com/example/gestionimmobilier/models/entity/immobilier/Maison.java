package com.example.gestionimmobilier.models.entity.immobilier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Maison extends BienImmobilier {

    @Column(name = "surface_terrain")
    private Double surfaceTerrain;

    @Column(nullable = false)
    private boolean garage;
}
