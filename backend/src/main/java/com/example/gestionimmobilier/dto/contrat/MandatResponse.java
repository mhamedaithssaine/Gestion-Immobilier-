package com.example.gestionimmobilier.dto.contrat;

import com.example.gestionimmobilier.models.enums.StatutMandat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MandatResponse(
        UUID id,
        String numMandat,
        UUID proprietaireId,
        String proprietaireNom,
        UUID agentId,
        String agentNom,
        UUID bienId,
        String bienReference,

        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal commissionPct,
        StatutMandat statut,
        LocalDate dateSignature,
        String documentUrl
) {}
