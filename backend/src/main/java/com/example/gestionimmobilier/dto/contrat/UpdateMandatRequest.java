package com.example.gestionimmobilier.dto.contrat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mise à jour partielle d'un mandat (admin). Champs null = inchangé.
 * Le document PDF se remplace via multipart ({@code document}), pas par URL.
 */
public record UpdateMandatRequest(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal commissionPct,
        LocalDate dateSignature
) {}
