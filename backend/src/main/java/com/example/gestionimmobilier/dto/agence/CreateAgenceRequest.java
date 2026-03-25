package com.example.gestionimmobilier.dto.agence;

import com.example.gestionimmobilier.validation.BusinessEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAgenceRequest(
        @NotBlank(message = "Le nom de l'agence est requis")
        @Size(max = 255)
        String nom,

        @NotBlank(message = "L'email de l'agence est requis")
        @Email(message = "L'email doit être valide")
        @BusinessEmail
        String email,

        @Size(max = 50)
        String telephone,

        @Size(max = 500)
        String adresse,

        @Size(max = 100)
        String ville,

        @NotBlank(message = "Le nom d'utilisateur du contact est requis")
        @Size(min = 2, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Le nom d'utilisateur ne doit contenir que des lettres, chiffres, '_' ou '-' (sans espaces).")
        String agentUsername,

        @NotBlank(message = "L'email du contact est requis")
        @Email(message = "L'email du contact doit être valide")
        String agentEmail,

        @NotBlank(message = "Le prénom du contact est requis")
        @Size(max = 255)
        String agentFirstName,

        @NotBlank(message = "Le nom du contact est requis")
        @Size(max = 255)
        String agentLastName,

        @NotBlank(message = "Le mot de passe du contact est requis")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String agentPassword
) {}
