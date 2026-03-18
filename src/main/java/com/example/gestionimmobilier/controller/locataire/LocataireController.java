package com.example.gestionimmobilier.controller.locataire;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.user.CreateLocataireRequest;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.UpdateLocataireRequest;
import com.example.gestionimmobilier.service.locataire.LocataireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/locataires")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class LocataireController {

    private final LocataireService locataireService;

    public LocataireController(LocataireService locataireService) {
        this.locataireService = locataireService;
    }

    @PostMapping
    public ResponseEntity<ApiRetour<LocataireResponse>> ajouterLocataire(
            @RequestBody @Valid CreateLocataireRequest request) {
        LocataireResponse locataire = locataireService.createLocataire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Locataire créé avec succès", locataire));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiRetour<LocataireResponse>> modifierLocataire(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateLocataireRequest request) {
        LocataireResponse locataire = locataireService.updateLocataire(id, request);
        return ResponseEntity.ok(ApiRetour.success("Locataire modifié avec succès", locataire));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRetour<Void>> supprimerLocataire(@PathVariable UUID id) {
        locataireService.deleteLocataire(id);
        return ResponseEntity.ok(ApiRetour.<Void>success("Locataire supprimé avec succès"));
    }
}
