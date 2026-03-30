package com.example.gestionimmobilier.controller.dashboard;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.dashboard.ProprietaireDashboardOverviewResponse;
import com.example.gestionimmobilier.service.dashboard.ProprietaireDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proprietaire/dashboard")
@PreAuthorize("hasAnyAuthority('ROLE_PROPRIETAIRE', 'ROLE_ADMIN')")
public class ProprietaireDashboardController {

    private final ProprietaireDashboardService proprietaireDashboardService;

    public ProprietaireDashboardController(ProprietaireDashboardService proprietaireDashboardService) {
        this.proprietaireDashboardService = proprietaireDashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiRetour<ProprietaireDashboardOverviewResponse>> overview(
            @RequestParam int annee,
            @RequestParam int mois,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        ProprietaireDashboardOverviewResponse data =
                proprietaireDashboardService.getOverview(keycloakId, annee, mois);
        return ResponseEntity.ok(ApiRetour.success("Tableau de bord propriétaire", data));
    }
}
