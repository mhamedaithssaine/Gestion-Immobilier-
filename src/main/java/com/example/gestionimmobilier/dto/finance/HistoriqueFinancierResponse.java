package com.example.gestionimmobilier.dto.finance;

import java.math.BigDecimal;
import java.util.List;

public record HistoriqueFinancierResponse(
        List<HistoriqueFinancierLigneResponse> lignes,
        BigDecimal totalVersements,
        int nombreVersements
) {}
