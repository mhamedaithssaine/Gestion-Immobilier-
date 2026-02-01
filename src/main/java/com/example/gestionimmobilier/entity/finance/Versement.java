package com.example.gestionimmobilier.entity.finance;

import com.example.gestionimmobilier.entity.base.BaseEntity;
import com.example.gestionimmobilier.entity.contrat.Bail;
import com.example.gestionimmobilier.entity.enums.ModeVersement;
import com.example.gestionimmobilier.entity.user.Proprietaire;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Versement extends BaseEntity {

    private LocalDateTime dateVersement;
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private ModeVersement mode;

    private String referencePaiement;
    private boolean valide;

    @ManyToOne
    private Bail bail;

    @ManyToOne
    private Proprietaire proprietaire;
}
