package com.example.gestionimmobilier.controller;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.immobilier.CreateBienRequest;
import com.example.gestionimmobilier.service.BienService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/proprietaire/biens")
public class BienImmobilierController {

    private final BienService bienService;

    public BienImmobilierController(BienService bienService) {
        this.bienService = bienService;
    }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('ROLE_PROPRIETAIRE')")
public ResponseEntity<ApiRetour<BienResponse>> creerBienJson(@RequestBody @Valid CreateBienRequest data) {
    String keycloakId = getCurrentKeycloakId();
    BienResponse bien = bienService.creerBien(keycloakId, data, new MultipartFile[0]);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiRetour.success("Bien immobilier créé avec succès", bien));
}

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_PROPRIETAIRE')")
    public ResponseEntity<ApiRetour<BienResponse>> modifierBien(
            @PathVariable UUID id,
            @RequestBody @Valid CreateBienRequest data) {
        String keycloakId = getCurrentKeycloakId();
        BienResponse bien = bienService.modifierBien(id, keycloakId, data, new MultipartFile[0]);
        return ResponseEntity.ok(ApiRetour.success("Bien immobilier modifié avec succès", bien));
    }

    private String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}
