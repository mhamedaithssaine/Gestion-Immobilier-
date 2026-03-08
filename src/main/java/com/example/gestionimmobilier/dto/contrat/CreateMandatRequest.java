package com.example.gestionimmobilier.dto.contrat;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMandatRequest(
        @NotNull UUID bienId,
        @NotNull UUID proprietaireId,
        @NotNull UUID agentId,

        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,

        BigDecimal commissionPct,

        LocalDate dateSignature,
        String documentUrl
) {}
