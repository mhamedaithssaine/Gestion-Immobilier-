package com.example.gestionimmobilier.mapper;

import com.example.gestionimmobilier.dto.immobilier.AdresseResponse;
import com.example.gestionimmobilier.dto.immobilier.AppartementResponse;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.immobilier.MaisonResponse;
import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.immobilier.Appartement;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.immobilier.Maison;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class BienMapper {

    public AdresseResponse toAdresseResponse(Adresse adresse) {
        if (adresse == null) return null;
        return new AdresseResponse(
                adresse.getId(),
                adresse.getRue(),
                adresse.getVille(),
                adresse.getCodePostal(),
                adresse.getPays()
        );
    }

   
    public BienResponse toBienResponse(BienImmobilier bien) {
        if (bien == null) return null;
        if (bien instanceof Appartement appartement) {
            return toAppartementResponse(appartement);
        }
        if (bien instanceof Maison maison) {
            return toMaisonResponse(maison);
        }
        throw new IllegalArgumentException("Type de bien non géré: " + bien.getClass().getSimpleName());
    }

    public AppartementResponse toAppartementResponse(Appartement bien) {
        return AppartementResponse.builder()
                .id(bien.getId())
                .reference(bien.getReference())
                .titre(bien.getTitre())
                .surface(bien.getSurface())
                .prixBase(bien.getPrixBase())
                .statut(bien.getStatut())
                .images(toImageUrls(bien.getImages()))
                .adresse(toAdresseResponse(bien.getAdresse()))
                .proprietaireId(bien.getProprietaire() != null ? bien.getProprietaire().getId() : null)
                .proprietaireNom(proprietaireNom(bien.getProprietaire()))
                .etage(bien.getEtage())
                .ascenseur(bien.isAscenseur())
                .build();
    }

    public MaisonResponse toMaisonResponse(Maison bien) {
        return MaisonResponse.builder()
                .id(bien.getId())
                .reference(bien.getReference())
                .titre(bien.getTitre())
                .surface(bien.getSurface())
                .prixBase(bien.getPrixBase())
                .statut(bien.getStatut())
                .images(toImageUrls(bien.getImages()))
                .adresse(toAdresseResponse(bien.getAdresse()))
                .proprietaireId(bien.getProprietaire() != null ? bien.getProprietaire().getId() : null)
                .proprietaireNom(proprietaireNom(bien.getProprietaire()))
                .surfaceTerrain(bien.getSurfaceTerrain())
                .garage(bien.isGarage())
                .build();
    }

    private List<String> toImageUrls(List<String> images) {
        if (images == null) return new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for (String path : images) {
            if (path == null || path.isBlank()) continue;
            urls.add(path.startsWith("/") ? path : "/uploads/" + path);
        }
        return urls;
    }

    private String proprietaireNom(com.example.gestionimmobilier.models.entity.user.Proprietaire p) {
        if (p == null) return "";
        return (p.getFirstName() + " " + p.getLastName()).trim();
    }
}
