package com.example.gestionimmobilier.controller.bienImmobilier;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.immobilier.CreateBienRequest;
import com.example.gestionimmobilier.service.bienImmobilier.BienService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Biens accessibles via mandat : lecture ({@code ROLE_AGENT} / {@code ROLE_ADMIN}),
 * mise à jour réservée à l’agent mandataire ({@code ROLE_AGENT}).
 */
@RestController
@RequestMapping("/api/agent/biens")
public class AgentBienController {

    private final BienService bienService;
    private final ObjectMapper objectMapper;

    public AgentBienController(BienService bienService, ObjectMapper objectMapper) {
        this.bienService = bienService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<BienResponse>>> listerMesBiensSousMandat() {
        String keycloakId = getCurrentKeycloakId();
        List<BienResponse> biens = bienService.listerBiens(keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Biens liés à vos mandats", biens));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<BienResponse>> getBien(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        BienResponse bien = bienService.getBienById(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Détail du bien", bien));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<BienResponse>> modifierBienJson(
            @PathVariable UUID id,
            @RequestBody @Valid CreateBienRequest data) {
        String keycloakId = getCurrentKeycloakId();
        BienResponse bien = bienService.modifierBienPourAgent(id, keycloakId, data, new MultipartFile[0]);
        return ResponseEntity.ok(ApiRetour.success("Bien immobilier modifié avec succès", bien));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<BienResponse>> modifierBienMultipart(
            @PathVariable UUID id,
            @RequestPart("data") String data,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) throws Exception {
        CreateBienRequest request = objectMapper.readValue(data, CreateBienRequest.class);
        String keycloakId = getCurrentKeycloakId();
        MultipartFile[] parts = images != null ? images : new MultipartFile[0];
        BienResponse bien = bienService.modifierBienPourAgent(id, keycloakId, request, parts);
        return ResponseEntity.ok(ApiRetour.success("Bien immobilier modifié avec succès", bien));
    }

    private static String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}
