package com.example.gestionimmobilier.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vue consolidée du tableau de bord agent (mandats, baux, locataires, trésorerie du mois).
 */
public record AgentDashboardOverviewResponse(
        long mandatsActifs,
        long mandatsTotal,
        long biensDistinctsSousMandat,
        long bauxActifs,
        long demandesEnAttenteValidationAgent,
        long locatairesActifsDistincts,
        BigDecimal revenusMensuels,
        int anneePeriode,
        int moisPeriode,
        List<LocataireEnRetardLigneResponse> locatairesEnRetard,
        /** Biens distincts sous mandat actif, par statut d’annonce (périmètre agent). */
        long biensDisponiblesSousMandat,
        long biensLouesSousMandat,
        long biensVendusSousMandat,
        long biensSousCompromisSousMandat,
        /** Demande de modification / clôture d’agence soumise à l’admin. */
        boolean demandeModificationAgenceEnAttente,
        String typeDemandeModificationAgenceEnAttente,
        String resumeDemandeModificationAgenceEnAttente,
        /** Dernier motif de refus admin sur une demande concernant l’agence (si présent). */
        String derniereNoteAdminDemandeAgence,
        LocalDateTime derniereNoteAdminDemandeAgenceLe
) {}
