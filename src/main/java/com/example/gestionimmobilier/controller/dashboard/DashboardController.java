package com.example.gestionimmobilier.controller.dashboard;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.dashboard.*;
import com.example.gestionimmobilier.service.dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AGENT')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/nombre-biens-disponibles")
    public ResponseEntity<ApiRetour<NombreBiensDisponiblesResponse>> getNombreBiensDisponibles() {
        NombreBiensDisponiblesResponse data = dashboardService.getNombreBiensDisponibles();
        return ResponseEntity.ok(ApiRetour.success("Nombre de biens disponibles", data));
    }

    @GetMapping("/biens-loues-vs-libres")
    public ResponseEntity<ApiRetour<BiensLouesVsLibresResponse>> getBiensLouesVsLibres() {
        BiensLouesVsLibresResponse data = dashboardService.getBiensLouesVsLibres();
        return ResponseEntity.ok(ApiRetour.success("Biens loués vs libres", data));
    }

    @GetMapping("/revenus-mensuels")
    public ResponseEntity<ApiRetour<RevenusMensuelsResponse>> getRevenusMensuels(
            @RequestParam int annee,
            @RequestParam int mois) {
        RevenusMensuelsResponse data = dashboardService.getRevenusMensuels(annee, mois);
        return ResponseEntity.ok(ApiRetour.success("Revenus mensuels", data));
    }

    @GetMapping("/locataires-en-retard")
    public ResponseEntity<ApiRetour<LocatairesEnRetardResponse>> getLocatairesEnRetard(
            @RequestParam int annee,
            @RequestParam int mois) {
        LocatairesEnRetardResponse data = dashboardService.getLocatairesEnRetard(annee, mois);
        return ResponseEntity.ok(ApiRetour.success("Locataires en retard de paiement", data));
    }

    @GetMapping("/agences/stats/mandats-gestion")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<MandatsGestionStatistiqueResponse>>> getStatistiquesMandatsToutesAgences() {
        List<MandatsGestionStatistiqueResponse> data = dashboardService.getStatistiquesMandatsToutesAgences();
        return ResponseEntity.ok(ApiRetour.success("Statistiques mandats de gestion par agence", data));
    }

    @GetMapping("/agences/{id}/mandats-gestion")
    public ResponseEntity<ApiRetour<MandatsGestionStatistiqueResponse>> getStatistiqueMandatsAgence(@PathVariable UUID id) {
        MandatsGestionStatistiqueResponse data = dashboardService.getStatistiqueMandatsPourAgence(id);
        return ResponseEntity.ok(ApiRetour.success("Statistiques mandats de gestion pour l'agence", data));
    }

    /** Statistiques des mandats de gestion pour un agent (id = id de l'agent, les mandats sont liés à l'agent). */
    @GetMapping("/agents/{agentId}/mandats-gestion")
    public ResponseEntity<ApiRetour<MandatsGestionParAgentResponse>> getStatistiqueMandatsAgent(@PathVariable UUID agentId) {
        MandatsGestionParAgentResponse data = dashboardService.getStatistiqueMandatsPourAgent(agentId);
        return ResponseEntity.ok(ApiRetour.success("Statistiques mandats de gestion pour l'agent", data));
    }
}
