package com.example.gestionimmobilier.controller.agence;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.agence.AgenceEspaceAgentResponse;
import com.example.gestionimmobilier.dto.agence.UpdateAgenceRequest;
import com.example.gestionimmobilier.service.agence.AgenceModificationDemandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/agence")
@PreAuthorize("hasAuthority('ROLE_AGENT')")
public class AgenceAgentController {

    private final AgenceModificationDemandeService modificationDemandeService;

    public AgenceAgentController(AgenceModificationDemandeService modificationDemandeService) {
        this.modificationDemandeService = modificationDemandeService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiRetour<AgenceEspaceAgentResponse>> getMonAgence() {
        AgenceEspaceAgentResponse data =
                modificationDemandeService.getEspaceAgent(getCurrentKeycloakId());
        return ResponseEntity.ok(ApiRetour.success("Fiche de votre agence", data));
    }

    @PostMapping("/me/demandes/mise-a-jour")
    public ResponseEntity<ApiRetour<AgenceEspaceAgentResponse>> soumettreMiseAJour(
            @RequestBody @Valid UpdateAgenceRequest request) {
        AgenceEspaceAgentResponse data =
                modificationDemandeService.soumettreMiseAJour(getCurrentKeycloakId(), request);
        return ResponseEntity.ok(ApiRetour.success("Demande enregistrée. En attente de validation admin.", data));
    }

    @PostMapping("/me/demandes/suppression")
    public ResponseEntity<ApiRetour<AgenceEspaceAgentResponse>> soumettreSuppression() {
        AgenceEspaceAgentResponse data =
                modificationDemandeService.soumettreSuppression(getCurrentKeycloakId());
        return ResponseEntity.ok(
                ApiRetour.success("Demande de clôture enregistrée. En attente de validation admin.", data));
    }

    private static String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}
