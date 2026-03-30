package com.example.gestionimmobilier.controller.proprietaire;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.UpdateContratRequest;
import com.example.gestionimmobilier.dto.contrat.UpdateContratStatutRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import jakarta.validation.Valid;
import com.example.gestionimmobilier.service.contrat.ContratService;
import com.example.gestionimmobilier.service.mandat.MandatService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proprietaire")
@PreAuthorize("hasAnyAuthority('ROLE_PROPRIETAIRE', 'ROLE_ADMIN')")
public class ProprietaireEspaceController {

    private final ContratService contratService;
    private final MandatService mandatService;

    public ProprietaireEspaceController(ContratService contratService, MandatService mandatService) {
        this.contratService = contratService;
        this.mandatService = mandatService;
    }

    @GetMapping("/contrats")
    public ResponseEntity<ApiRetour<List<ContratResponse>>> listerMesContrats() {
        String keycloakId = getCurrentKeycloakId();
        List<ContratResponse> contrats = contratService.listerContratsParProprietaireKeycloak(keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Liste de vos contrats", contrats));
    }

    @GetMapping("/mandats")
    public ResponseEntity<ApiRetour<List<MandatResponse>>> listerMesMandats() {
        String keycloakId = getCurrentKeycloakId();
        List<MandatResponse> mandats = mandatService.listerMandatsParProprietaireKeycloak(keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Liste de vos mandats", mandats));
    }

    @GetMapping("/mandats/{id}")
    public ResponseEntity<ApiRetour<MandatResponse>> getMonMandat(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        MandatResponse mandat = mandatService.getMandatByIdPourProprietaire(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Détail du mandat", mandat));
    }

    @GetMapping("/mandats/{id}/document")
    public ResponseEntity<byte[]> telechargerDocumentMandat(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        MandatService.MandatDocumentFile file = mandatService.getMandatDocumentForProprietaire(id, keycloakId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.content());
    }

    @PatchMapping("/contrats/{id}/resilier")
    public ResponseEntity<ApiRetour<ContratResponse>> resilierMonContrat(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.resilierContratProprietaire(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Contrat résilié avec succès", contrat));
    }

    @PatchMapping("/contrats/{id}")
    public ResponseEntity<ApiRetour<ContratResponse>> modifierMonContrat(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContratRequest request) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.modifierContratProprietaire(id, keycloakId, request);
        return ResponseEntity.ok(ApiRetour.success("Contrat modifié avec succès", contrat));
    }

    @PatchMapping("/contrats/{id}/statut")
    public ResponseEntity<ApiRetour<ContratResponse>> changerStatutMonContrat(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContratStatutRequest request) {
        String keycloakId = getCurrentKeycloakId();
        ContratResponse contrat = contratService.changerStatutContratProprietaire(id, keycloakId, request.statut());
        return ResponseEntity.ok(ApiRetour.success("Statut du contrat mis à jour", contrat));
    }

    @PatchMapping("/mandats/{id}/demande-resiliation")
    public ResponseEntity<ApiRetour<MandatResponse>> demanderResiliationMonMandat(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        MandatResponse mandat = mandatService.demanderResiliationProprietaire(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Demande de résiliation transmise à l'administrateur", mandat));
    }

    private static String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}

