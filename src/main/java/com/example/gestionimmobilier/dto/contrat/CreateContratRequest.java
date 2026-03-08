package com.example.gestionimmobilier.dto.contrat;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContratRequest(
        @NotNull UUID bienId,
        @NotNull UUID proprietaireId,
        @NotNull UUID locataireId,
        UUID agentId,

        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,

        @NotNull BigDecimal loyerHC,
        @NotNull BigDecimal charges,

        LocalDate dateSignature,
        String documentUrl
) {}

