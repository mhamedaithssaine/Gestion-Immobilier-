package com.example.gestionimmobilier.dto.immobilier;

import com.example.gestionimmobilier.models.enums.StatutBien;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Réponse abstraite pour un bien immobilier.
 * Le type concret reflète l'héritage des entités (Appartement / Maison).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AppartementResponse.class, name = "APPARTEMENT"),
        @JsonSubTypes.Type(value = MaisonResponse.class, name = "MAISON")
})
public abstract class BienResponse {

    public abstract String getType();
    public abstract UUID getId();
    public abstract String getReference();
    public abstract String getTitre();
    public abstract Double getSurface();
    public abstract BigDecimal getPrixBase();
    public abstract StatutBien getStatut();
    public abstract List<String> getImages();
    public abstract AdresseResponse getAdresse();
    public abstract UUID getProprietaireId();
    public abstract String getProprietaireNom();
}
