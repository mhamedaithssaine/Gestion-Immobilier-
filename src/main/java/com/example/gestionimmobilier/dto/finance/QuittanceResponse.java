package com.example.gestionimmobilier.dto.finance;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuittanceResponse(
        UUID id,
        String referenceQuittance,
        int mois,
        int annee,
        String urlPdf,
        LocalDateTime dateGeneration,
        UUID versementId,
        UUID proprietaireId
) {}
