package com.example.gestionimmobilier.controller.mandat;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.CreateMandatRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.dto.contrat.UpdateMandatRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.service.mandat.MandatService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/mandats")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class MandatController {

    private final MandatService mandatService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(MandatController.class);


    public MandatController(MandatService mandatService, ObjectMapper objectMapper) {
        this.mandatService = mandatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiRetour<MandatResponse>> creerMandat(
            @RequestBody @Valid CreateMandatRequest request) {

        log.info("Demande création mandat pour propriétaire {} et bien {}",
                request.proprietaireId(), request.bienId());

        MandatResponse mandat = mandatService.creerMandat(request);

        log.info("Mandat créé avec succès id={} numéro={}",
                mandat.id(), mandat.numMandat());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Mandat de gestion créé avec succès", mandat));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiRetour<MandatResponse>> creerMandatMultipart(
            @RequestPart("data") String data,
            @RequestPart(value = "document", required = false) MultipartFile document
    ) throws Exception {
        CreateMandatRequest request = objectMapper.readValue(data, CreateMandatRequest.class);
        MandatResponse mandat = mandatService.creerMandat(request, document);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Mandat de gestion créé avec succès", mandat));
    }

    @GetMapping
    public ResponseEntity<ApiRetour<List<MandatResponse>>> listerMandats(
            @RequestParam(required = false) StatutMandat statut,
            @RequestParam(required = false) UUID proprietaireId,
            @RequestParam(required = false) UUID bienId) {

        log.info("Recherche mandats statut={} proprietaireId={} bienId={}", statut, proprietaireId, bienId);

        List<MandatResponse> mandats = mandatService.listerMandats(statut, proprietaireId, bienId);

        log.info("Nombre de mandats trouvés {}", mandats.size());

        return ResponseEntity.ok(ApiRetour.success("Liste des mandats", mandats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRetour<MandatResponse>> getMandat(@PathVariable UUID id) {

        log.info("Consultation mandat id={}", id);

        MandatResponse mandat = mandatService.getMandatById(id);

        log.info("Mandat récupéré id={} numéro={}", mandat.id(), mandat.numMandat());
        return ResponseEntity.ok(ApiRetour.success("Détail du mandat", mandat));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiRetour<MandatResponse>> mettreAJourMandat(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateMandatRequest request) {
        log.info("Mise à jour mandat id={}", id);
        MandatResponse mandat = mandatService.mettreAJourMandatAdmin(id, request, null);
        return ResponseEntity.ok(ApiRetour.success("Mandat mis à jour", mandat));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiRetour<MandatResponse>> mettreAJourMandatMultipart(
            @PathVariable UUID id,
            @RequestPart("data") String data,
            @RequestPart(value = "document", required = false) MultipartFile document
    ) throws Exception {
        log.info("Mise à jour mandat (multipart) id={}", id);
        UpdateMandatRequest request = objectMapper.readValue(data, UpdateMandatRequest.class);
        MandatResponse mandat = mandatService.mettreAJourMandatAdmin(id, request, document);
        return ResponseEntity.ok(ApiRetour.success("Mandat mis à jour", mandat));
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> telechargerDocument(@PathVariable UUID id) {
        MandatService.MandatDocumentFile file = mandatService.getMandatDocumentForAdmin(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.content());
    }

    @GetMapping("/proprietaires/{proprietaireId}/biens")
    public ResponseEntity<ApiRetour<List<BienResponse>>> listerBiensParProprietaire(@PathVariable UUID proprietaireId) {
        log.info("Chargement biens pour création mandat proprietaireId={}", proprietaireId);
        List<BienResponse> biens = mandatService.listerBiensParProprietaire(proprietaireId);
        return ResponseEntity.ok(ApiRetour.success("Biens du propriétaire", biens));
    }

    @PatchMapping("/{id}/resilier")
    public ResponseEntity<ApiRetour<MandatResponse>> resilierMandat(@PathVariable UUID id) {
        log.info("Demande résiliation mandat id={}", id);
        MandatResponse mandat = mandatService.resilierMandat(id);
        log.info("Mandat résilié id={} numéro={}", mandat.id(), mandat.numMandat());

        return ResponseEntity.ok(ApiRetour.success("Mandat résilié avec succès", mandat));
    }

    @PatchMapping("/{id}/approuver-resiliation")
    public ResponseEntity<ApiRetour<MandatResponse>> approuverResiliation(@PathVariable UUID id) {
        log.info("Approbation résiliation mandat id={}", id);
        MandatResponse mandat = mandatService.approuverResiliationAdmin(id);
        return ResponseEntity.ok(ApiRetour.success("Résiliation approuvée", mandat));
    }

    @PatchMapping("/{id}/rejeter-demande-resiliation")
    public ResponseEntity<ApiRetour<MandatResponse>> rejeterDemandeResiliation(@PathVariable UUID id) {
        log.info("Rejet demande résiliation mandat id={}", id);
        MandatResponse mandat = mandatService.rejeterDemandeResiliationAdmin(id);
        return ResponseEntity.ok(ApiRetour.success("Demande de résiliation rejetée, mandat reste actif", mandat));
    }
}
