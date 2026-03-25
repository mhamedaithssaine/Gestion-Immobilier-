package com.example.gestionimmobilier.dto.immobilier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdresseRequest(
        @NotBlank(message = "La rue est requise")
        @Size(max = 255)
        String rue,

        @NotBlank(message = "La ville est requise")
        @Size(max = 100)
        String ville,

        @NotBlank(message = "Le code postal est requis")
        @Size(max = 20)
        String codePostal,

        @NotBlank(message = "Le pays est requis")
        @Size(max = 100)
        String pays
) {}
