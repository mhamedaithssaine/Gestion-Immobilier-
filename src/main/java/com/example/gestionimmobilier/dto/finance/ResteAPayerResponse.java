package com.example.gestionimmobilier.dto.finance;

import java.math.BigDecimal;
import java.util.UUID;

public record ResteAPayerResponse(
        UUID contratId,
        int annee,
        int mois,
        BigDecimal loyerDuMois,
        BigDecimal totalVersements,
        BigDecimal resteAPayer
) {}
