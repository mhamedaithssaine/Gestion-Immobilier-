package com.example.gestionimmobilier.dto.contrat;

import com.example.gestionimmobilier.models.enums.StatutMandat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Réponse mandat. {@code documentUrl} est l’URL Cloudinary persistée (affichage / traçabilité) ;
 * pour un fichier garanti avec l’auth applicative, le front doit utiliser
 * {@code GET .../mandats/{id}/document} (admin, agent ou espace propriétaire selon le rôle).
 */
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
