package com.example.gestionimmobilier.dto.user;

import com.example.gestionimmobilier.models.enums.StatutDossier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateLocataireRequest(
        @NotBlank(message = "Le nom d'utilisateur est requis")
        @Size(min = 2, max = 100)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Le nom d'utilisateur ne doit contenir que des lettres, chiffres, '_' ou '-' (sans espaces)."
        )
        String username,

        @NotBlank(message = "L'email est requis")
        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le prénom est requis")
        @Size(max = 255)
        String firstName,

        @NotBlank(message = "Le nom est requis")
        @Size(max = 255)
        String lastName,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String password,

        BigDecimal budgetMax,

        StatutDossier statutDossier
) {}
