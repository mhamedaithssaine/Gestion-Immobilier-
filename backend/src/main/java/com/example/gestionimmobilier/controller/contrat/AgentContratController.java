package com.example.gestionimmobilier.controller.contrat;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.UpdateContratRequest;
import com.example.gestionimmobilier.dto.contrat.UpdateContratStatutRequest;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.service.contrat.ContratService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent/contrats")
@PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMIN')")
public class AgentContratController {

    private final ContratService contratService;

    public AgentContratController(ContratService contratService) {
        this.contratService = contratService;
    }

    @GetMapping
    public ResponseEntity<ApiRetour<List<ContratResponse>>> listerMesContrats(
            @RequestParam(required = false) StatutBail statut) {
        String keycloakId = getCurrentKeycloakId();
        List<ContratResponse> contrats = contratService.listerContratsParAgentKeycloak(keycloakId, statut);
        return ResponseEntity.ok(ApiRetour.success("Liste de vos baux et demandes (mandat)", contrats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRetour<ContratResponse>> getContrat(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.getContratByIdPourAgent(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Détail du bail", contrat));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiRetour<ContratResponse>> modifierContrat(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContratRequest request) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.modifierContratAgent(id, keycloakId, request);
        return ResponseEntity.ok(ApiRetour.success("Contrat modifié avec succès", contrat));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ApiRetour<ContratResponse>> changerStatut(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContratStatutRequest request) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.changerStatutContratAgent(id, keycloakId, request.statut());
        return ResponseEntity.ok(ApiRetour.success("Statut du contrat mis à jour", contrat));
    }

    private static String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}
