package com.example.gestionimmobilier.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
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
        String lastName
) {}

