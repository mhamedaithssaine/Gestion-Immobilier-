package com.example.gestionimmobilier.dto.immobilier;

import com.example.gestionimmobilier.models.enums.StatutBien;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Réponse pour un appartement. Alignée sur l'entité {@link com.example.gestionimmobilier.models.entity.immobilier.Appartement}.
 */
@JsonTypeName("APPARTEMENT")
@Builder
public class AppartementResponse extends BienResponse {

    UUID id;
    String reference;
    String titre;
    Double surface;
    BigDecimal prixBase;
    StatutBien statut;
    List<String> images;
    AdresseResponse adresse;
    UUID proprietaireId;
    String proprietaireNom;

    int etage;
    boolean ascenseur;

    @Override
    public UUID getId() { return id; }
    @Override
    public String getReference() { return reference; }
    @Override
    public String getTitre() { return titre; }
    @Override
    public Double getSurface() { return surface; }
    @Override
    public BigDecimal getPrixBase() { return prixBase; }
    @Override
    public StatutBien getStatut() { return statut; }
    @Override
    public List<String> getImages() { return images; }
    @Override
    public AdresseResponse getAdresse() { return adresse; }
    @Override
    public UUID getProprietaireId() { return proprietaireId; }
    @Override
    public String getProprietaireNom() { return proprietaireNom; }
}
