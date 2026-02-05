package com.example.gestionimmobilier.entity.intervention;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.immobilier.BienImmobilier;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "interventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Intervention extends BaseEntity {

    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 1000)
    private String commentaire;

    @Column(name = "est_resolue", nullable = false)
    private boolean estResolue;

    @ManyToOne
    @JoinColumn(name = "bien_id", nullable = false)
    private BienImmobilier bien;
}
