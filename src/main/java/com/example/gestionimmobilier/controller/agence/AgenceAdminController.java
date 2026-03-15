package com.example.gestionimmobilier.controller.agence;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.service.agence.AgenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/agences")
public class AgenceAdminController {

    private final AgenceService agenceService;

    public AgenceAdminController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<AgenceResponse>>> listerToutes() {
        List<AgenceResponse> agences = agenceService.listerToutes();
        return ResponseEntity.ok(ApiRetour.success("Liste des agences", agences));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<AgenceResponse>>> listerEnAttente() {
        List<AgenceResponse> agences = agenceService.listerEnAttente();
        return ResponseEntity.ok(ApiRetour.success("Agences en attente d'approbation", agences));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<AgenceResponse>> getById(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.getById(id);
        return ResponseEntity.ok(ApiRetour.success("Détail de l'agence", agence));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<AgenceResponse>> approuver(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.approuver(id);
        return ResponseEntity.ok(ApiRetour.success("Agence approuvée avec succès", agence));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<AgenceResponse>> rejeter(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.rejeter(id);
        return ResponseEntity.ok(ApiRetour.success("Agence rejetée", agence));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<AgenceResponse>> suspendre(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.suspendre(id);
        return ResponseEntity.ok(ApiRetour.success("Agence suspendue", agence));
    }
}
