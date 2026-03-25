package com.example.gestionimmobilier.dto.immobilier;

import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.immobilier.Maison;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;

/**
 * Requête de création d'une maison. Alignée sur l'entité {@link Maison}.
 */
@JsonTypeName("MAISON")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateMaisonRequest extends CreateBienRequest {

    private Double surfaceTerrain;
    private boolean garage;

    @Override
    public BienImmobilier toEntity(Adresse adresse, Proprietaire proprietaire, String reference, String createdBy) {
        double terrain = surfaceTerrain != null ? surfaceTerrain : 0.0;
        Maison entity = Maison.builder()
                .reference(reference)
                .titre(getTitre())
                .surface(getSurface())
                .prixBase(getPrixBase())
                .statut(getStatut())
                .adresse(adresse)
                .proprietaire(proprietaire)
                .surfaceTerrain(terrain)
                .garage(garage)
                .images(new ArrayList<>())
                .build();
        entity.setCreatedBy(createdBy);
        return entity;
    }
}
