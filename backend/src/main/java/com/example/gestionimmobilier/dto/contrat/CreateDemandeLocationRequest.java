package com.example.gestionimmobilier.dto.contrat;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDemandeLocationRequest(
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin
) {
}

