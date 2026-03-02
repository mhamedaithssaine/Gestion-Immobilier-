package com.example.gestionimmobilier.dto.immobilier;

import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Requête abstraite de création d'un bien immobilier.
 * Le type concret (Appartement / Maison) est déterminé par le polymorphisme JSON
 * via {@link JsonTypeInfo} et reflète l'héritage des entités.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateAppartementRequest.class, name = "APPARTEMENT"),
        @JsonSubTypes.Type(value = CreateMaisonRequest.class, name = "MAISON")
})
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class CreateBienRequest {

    @NotBlank(message = "Le titre est requis")
    @Size(max = 255)
    private String titre;

    @NotNull(message = "La surface est requise")
    @Positive(message = "La surface doit être positive")
    private Double surface;

    @NotNull(message = "Le prix de base est requis")
    @DecimalMin(value = "0", inclusive = false, message = "Le prix doit être strictement positif")
    private BigDecimal prixBase;

    @NotNull(message = "Le statut est requis")
    private StatutBien statut;

    @NotNull(message = "L'adresse est requise")
    @Valid
    private AdresseRequest adresse;

    
    public abstract BienImmobilier toEntity(Adresse adresse, Proprietaire proprietaire, String reference, String createdBy);
}
