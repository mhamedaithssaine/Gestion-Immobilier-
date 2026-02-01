package com.example.gestionimmobilier.entity.intervention;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.immobilier.BienImmobilier;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

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
public class Intervention extends BaseEntity {

    private LocalDateTime dateSignalement;
    private String description;
    private String commentaire;
    private boolean estResolue;

    @ManyToOne
    private BienImmobilier bien;
}
