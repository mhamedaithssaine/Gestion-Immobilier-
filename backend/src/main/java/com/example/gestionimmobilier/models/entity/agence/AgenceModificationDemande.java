package com.example.gestionimmobilier.models.entity.agence;

import com.example.gestionimmobilier.models.entity.base.BaseEntity;
import com.example.gestionimmobilier.models.enums.StatutDemandeModificationAgence;
import com.example.gestionimmobilier.models.enums.TypeDemandeModificationAgence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "agence_modification_demandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AgenceModificationDemande extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDemandeModificationAgence type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemandeModificationAgence statut = StatutDemandeModificationAgence.EN_ATTENTE;

    @Column(length = 255)
    private String nomPropose;

    @Column(length = 255)
    private String emailPropose;

    @Column(length = 50)
    private String telephonePropose;

    @Column(length = 500)
    private String adressePropose;

    @Column(length = 100)
    private String villePropose;

    @Column(length = 64)
    private String demandeurKeycloakId;

    private LocalDateTime resoluLe;

    @Column(length = 500)
    private String commentaireAdmin;
}
