package com.example.gestionimmobilier.controller.finance;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.finance.CreateVersementRequest;
import com.example.gestionimmobilier.dto.finance.VersementResponse;
import com.example.gestionimmobilier.service.finance.VersementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.gestionimmobilier.exception.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/versements")
public class VersementController {

    private final VersementService versementService;
    private final ObjectMapper objectMapper;


    public VersementController(VersementService versementService,
    ObjectMapper objectMapper) {
        this.versementService = versementService;
        this.objectMapper = objectMapper;
    }

    /**
     * Création de versement SANS preuve (JSON simple).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<VersementResponse>> createVersementJson(
            @RequestBody @Valid CreateVersementRequest request) {
        VersementResponse response = versementService.createVersement(request, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Versement enregistré et quittance générée", response));
    }

    /**
     * Création de versement AVEC preuve (multipart/form-data).
     */
   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<VersementResponse>> createVersementMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "preuvePaiement", required = false) MultipartFile preuvePaiement) {

        CreateVersementRequest request;
        try {
            request = objectMapper.readValue(dataJson, CreateVersementRequest.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON invalide pour le champ 'data'");
        }

        VersementResponse response = versementService.createVersement(request, preuvePaiement);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Versement enregistré et quittance générée", response));
    }

    @GetMapping("/contrats/{contratId}/versements")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<List<VersementResponse>>> getVersementsByContrat(
            @PathVariable UUID contratId) {

        List<VersementResponse> versements = versementService.getVersementsByContrat(contratId);

        return ResponseEntity.ok(
                ApiRetour.success("Liste des versements du contrat", versements)
        );
    }
}
