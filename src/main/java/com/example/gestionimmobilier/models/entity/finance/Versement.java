package com.example.gestionimmobilier.entity.finance;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.contrat.Bail;
import com.example.gestionimmobilier.entity.enums.ModeVersement;
import com.example.gestionimmobilier.entity.user.Proprietaire;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "versements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Versement extends BaseEntity {

    @Column(name = "date_versement", nullable = false)
    private LocalDateTime dateVersement;

    @Column(nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeVersement mode;

    @Column(name = "reference_paiement", unique = true)
    private String referencePaiement;

    @Column(nullable = false)
    private boolean valide;

    @ManyToOne
    @JoinColumn(name = "bail_id", nullable = false)
    private Bail bail;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;
}
