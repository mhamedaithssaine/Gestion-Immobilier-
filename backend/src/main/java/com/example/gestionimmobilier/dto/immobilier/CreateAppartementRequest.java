package com.example.gestionimmobilier.dto.immobilier;

import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.immobilier.Appartement;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;

/**
 * Requête de création d'un appartement. Alignée sur l'entité {@link Appartement}.
 */
@JsonTypeName("APPARTEMENT")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateAppartementRequest extends CreateBienRequest {

    private int etage;
    private boolean ascenseur;

    @Override
    public BienImmobilier toEntity(Adresse adresse, Proprietaire proprietaire, String reference, String createdBy) {
        Appartement entity = Appartement.builder()
                .reference(reference)
                .titre(getTitre())
                .surface(getSurface())
                .prixBase(getPrixBase())
                .statut(getStatut())
                .adresse(adresse)
                .proprietaire(proprietaire)
                .etage(etage)
                .ascenseur(ascenseur)
                .images(new ArrayList<>())
                .build();
        entity.setCreatedBy(createdBy);
        return entity;
    }
}
