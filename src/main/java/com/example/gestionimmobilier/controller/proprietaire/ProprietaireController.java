package com.example.gestionimmobilier.controller.proprietaire;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.user.CreateProprietaireRequest;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.dto.user.UpdateProprietaireRequest;
import com.example.gestionimmobilier.service.proprietaire.ProprietaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/proprietaires")
public class ProprietaireController {

    private final ProprietaireService proprietaireService;

    public ProprietaireController(ProprietaireService proprietaireService) {
        this.proprietaireService = proprietaireService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<ProprietaireResponse>> ajouterProprietaire(
            @RequestBody @Valid CreateProprietaireRequest request) {
        ProprietaireResponse proprietaire = proprietaireService.createProprietaire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Propriétaire créé avec succès", proprietaire));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<ProprietaireResponse>> modifierProprietaire(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProprietaireRequest request) {
        ProprietaireResponse proprietaire = proprietaireService.updateProprietaire(id, request);
        return ResponseEntity.ok(ApiRetour.success("Propriétaire modifié avec succès", proprietaire));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<Void>> supprimerProprietaire(@PathVariable UUID id) {
        proprietaireService.deleteProprietaire(id);
        return ResponseEntity.ok(ApiRetour.<Void>success("Propriétaire supprimé avec succès"));
    }
}
