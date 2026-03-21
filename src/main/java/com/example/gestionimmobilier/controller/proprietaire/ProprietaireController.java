package com.example.gestionimmobilier.controller.proprietaire;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.finance.HistoriqueFinancierResponse;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.user.CreateProprietaireRequest;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.dto.user.UpdateProprietaireRequest;
import com.example.gestionimmobilier.service.bienImmobilier.BienService;
import com.example.gestionimmobilier.service.proprietaire.ProprietaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/proprietaires")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ProprietaireController {

    private final ProprietaireService proprietaireService;
    private final BienService bienService;

    public ProprietaireController(ProprietaireService proprietaireService, BienService bienService) {
        this.proprietaireService = proprietaireService;
        this.bienService = bienService;
    }

    @PostMapping
    public ResponseEntity<ApiRetour<ProprietaireResponse>> ajouterProprietaire(
            @RequestBody @Valid CreateProprietaireRequest request) {
        ProprietaireResponse proprietaire = proprietaireService.createProprietaire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Propriétaire créé avec succès", proprietaire));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiRetour<ProprietaireResponse>> modifierProprietaire(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProprietaireRequest request) {
        ProprietaireResponse proprietaire = proprietaireService.updateProprietaire(id, request);
        return ResponseEntity.ok(ApiRetour.success("Propriétaire modifié avec succès", proprietaire));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRetour<Void>> supprimerProprietaire(@PathVariable UUID id) {
        proprietaireService.deleteProprietaire(id);
        return ResponseEntity.ok(ApiRetour.<Void>success("Propriétaire supprimé avec succès"));
    }

    @PutMapping("/{proprietaireId}/biens/{bienId}")
    public ResponseEntity<ApiRetour<BienResponse>> associerBienAProprietaire(
            @PathVariable UUID proprietaireId,
            @PathVariable UUID bienId) {
        BienResponse bien = bienService.associerBienAProprietaire(bienId, proprietaireId);
        return ResponseEntity.ok(ApiRetour.success("Bien associé au propriétaire avec succès", bien));
    }

    @GetMapping("/{id}/historique-financier")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PROPRIETAIRE')")
    public ResponseEntity<ApiRetour<HistoriqueFinancierResponse>> getHistoriqueFinancier(@PathVariable UUID id) {
        HistoriqueFinancierResponse historique = proprietaireService.getHistoriqueFinancier(id);
        return ResponseEntity.ok(ApiRetour.success("Historique financier du propriétaire", historique));
    }
}
