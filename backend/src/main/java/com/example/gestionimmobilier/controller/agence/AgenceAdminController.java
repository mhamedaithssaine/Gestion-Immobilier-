package com.example.gestionimmobilier.controller.agence;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.dto.agence.UpdateAgenceRequest;
import com.example.gestionimmobilier.service.agence.AgenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/agences")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AgenceAdminController {

    private final AgenceService agenceService;

    public AgenceAdminController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    @GetMapping
    public ResponseEntity<ApiRetour<List<AgenceResponse>>> listerToutes() {
        List<AgenceResponse> agences = agenceService.listerToutes();
        return ResponseEntity.ok(ApiRetour.success("Liste des agences", agences));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiRetour<List<AgenceResponse>>> listerEnAttente() {
        List<AgenceResponse> agences = agenceService.listerEnAttente();
        return ResponseEntity.ok(ApiRetour.success("Agences en attente d'approbation", agences));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRetour<AgenceResponse>> getById(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.getById(id);
        return ResponseEntity.ok(ApiRetour.success("Détail de l'agence", agence));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiRetour<AgenceResponse>> approuver(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.approuver(id);
        return ResponseEntity.ok(ApiRetour.success("Agence approuvée avec succès", agence));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiRetour<AgenceResponse>> rejeter(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.rejeter(id);
        return ResponseEntity.ok(ApiRetour.success("Agence rejetée", agence));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiRetour<AgenceResponse>> suspendre(@PathVariable UUID id) {
        AgenceResponse agence = agenceService.suspendre(id);
        return ResponseEntity.ok(ApiRetour.success("Agence suspendue", agence));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiRetour<AgenceResponse>> modifier(@PathVariable UUID id, @RequestBody @Valid UpdateAgenceRequest request) {
        AgenceResponse agence = agenceService.mettreAJourAgenceAdmin(id, request);
        return ResponseEntity.ok(ApiRetour.success("Agence modifiée avec succès", agence));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRetour<Void>> supprimer(@PathVariable UUID id) {
        agenceService.supprimerAgenceAdmin(id);
        return ResponseEntity.ok(ApiRetour.success("Agence supprimée avec succès", null));
    }
}
