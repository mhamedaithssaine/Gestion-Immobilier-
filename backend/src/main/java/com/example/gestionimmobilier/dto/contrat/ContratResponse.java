package com.example.gestionimmobilier.dto.contrat;

import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.models.enums.StatutBail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratResponse(
        UUID id,
        String numContrat,
        LocalDate dateSignature,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal loyerHC,
        BigDecimal charges,
        String documentUrl,
        StatutBail statut,

        BienResponse bien,
        ProprietaireResponse proprietaire,
        LocataireResponse locataire,
        UUID agentId,
        String agentNomComplet
) {}

