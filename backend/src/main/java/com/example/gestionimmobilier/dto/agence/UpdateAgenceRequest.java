package com.example.gestionimmobilier.dto.agence;

import com.example.gestionimmobilier.validation.BusinessEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Mise à jour partielle de la fiche agence par un agent (champs null ou vides = inchangés).
 */
public record UpdateAgenceRequest(
        @Size(max = 255)
        String nom,

        @Email(message = "L'email doit être valide")
        @BusinessEmail
        String email,

        @Size(max = 50)
        String telephone,

        @Size(max = 500)
        String adresse,

        @Size(max = 100)
        String ville
) {}
