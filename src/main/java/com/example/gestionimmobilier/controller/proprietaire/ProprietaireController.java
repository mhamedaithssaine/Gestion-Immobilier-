package com.example.gestionimmobilier.controller.proprietaire;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.user.CreateProprietaireRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.service.proprietaire.ProprietaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/proprietaires")
public class ProprietaireController {

    private final ProprietaireService proprietaireService;

    public ProprietaireController(ProprietaireService proprietaireService) {
        this.proprietaireService = proprietaireService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<UtilisateurResponse>> ajouterProprietaire(
            @RequestBody @Valid CreateProprietaireRequest request) {
        UtilisateurResponse proprietaire = proprietaireService.createProprietaire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Propriétaire créé avec succès", proprietaire));
    }
}
