package com.example.gestionimmobilier.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Synthèse patrimoine, contrats, mandats et trésorerie pour le tableau de bord propriétaire.
 */
public record ProprietaireDashboardOverviewResponse(
        long biensTotal,
        long biensDisponibles,
        long biensLoues,
        long biensVendus,
        long biensSousCompromis,
        BigDecimal patrimoineEstimeAnnonce,
        long contratsTotal,
        long contratsActifs,
        long contratsEnAttenteCandidatures,
        long mandatsTotal,
        long mandatsActifs,
        long mandatsEnAttente,
        BigDecimal loyerMensuelTheoriqueContratsActifs,
        BigDecimal revenusEncaissesMois,
        int tauxOccupationContratsSurBiens,
        int anneePeriode,
        int moisPeriode,
        List<MoisMontantResponse> historiqueRevenus6Mois
) {}
