package com.example.gestionimmobilier.controller.mandat;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.service.mandat.MandatService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent/mandats")
@PreAuthorize("hasAuthority('ROLE_AGENT')")
public class AgentMandatController {

    private final MandatService mandatService;

    public AgentMandatController(MandatService mandatService) {
        this.mandatService = mandatService;
    }

    @GetMapping
    public ResponseEntity<ApiRetour<List<MandatResponse>>> listerMesMandats(
            @RequestParam(required = false) StatutMandat statut,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        List<MandatResponse> mandats = mandatService.listerMandatsPourAgent(keycloakId, statut);
        return ResponseEntity.ok(ApiRetour.success("Liste de vos mandats", mandats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRetour<MandatResponse>> getMandat(
            @PathVariable UUID id,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        MandatResponse mandat = mandatService.getMandatByIdPourAgent(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Détail du mandat", mandat));
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> telechargerDocument(
            @PathVariable UUID id,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        MandatService.MandatDocumentFile file = mandatService.getMandatDocumentForAgent(id, keycloakId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.content());
    }

    @PatchMapping("/{id}/demande-resiliation")
    public ResponseEntity<ApiRetour<MandatResponse>> demanderResiliation(
            @PathVariable UUID id,
            Authentication authentication) {
        String keycloakId = ((Jwt) authentication.getPrincipal()).getSubject();
        MandatResponse mandat = mandatService.demanderResiliationAgent(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Demande de résiliation transmise à l'administrateur", mandat));
    }
}
