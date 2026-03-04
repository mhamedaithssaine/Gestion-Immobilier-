package com.example.gestionimmobilier.controller.locataire;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.user.CreateLocataireRequest;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.service.locataire.LocataireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/locataires")
public class LocataireController {

    private final LocataireService locataireService;

    public LocataireController(LocataireService locataireService) {
        this.locataireService = locataireService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<LocataireResponse>> ajouterLocataire(
            @RequestBody @Valid CreateLocataireRequest request) {
        LocataireResponse locataire = locataireService.createLocataire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Locataire créé avec succès", locataire));
    }
}
