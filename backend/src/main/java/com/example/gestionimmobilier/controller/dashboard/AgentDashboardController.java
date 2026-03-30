package com.example.gestionimmobilier.controller.dashboard;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.dashboard.AgentDashboardOverviewResponse;
import com.example.gestionimmobilier.service.dashboard.AgentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/dashboard")
@PreAuthorize("hasAuthority('ROLE_AGENT')")
public class AgentDashboardController {

    private final AgentDashboardService agentDashboardService;

    public AgentDashboardController(AgentDashboardService agentDashboardService) {
        this.agentDashboardService = agentDashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiRetour<AgentDashboardOverviewResponse>> overview(
            @RequestParam int annee,
            @RequestParam int mois,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        AgentDashboardOverviewResponse data = agentDashboardService.getOverview(keycloakId, annee, mois);
        return ResponseEntity.ok(ApiRetour.success("Tableau de bord agence", data));
    }
}
