package com.example.gestionimmobilier.dto.finance;

import com.example.gestionimmobilier.models.enums.ModeVersement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoriqueFinancierLigneResponse(
        UUID versementId,
        LocalDateTime dateVersement,
        BigDecimal montant,
        ModeVersement mode,
        String numContrat,
        String referencePaiement
) {}
