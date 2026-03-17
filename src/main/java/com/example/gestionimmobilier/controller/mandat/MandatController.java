package com.example.gestionimmobilier.controller.mandat;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.CreateMandatRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.service.mandat.MandatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/mandats")
public class MandatController {

    private final MandatService mandatService;
    private static final Logger log = LoggerFactory.getLogger(MandatController.class);


    public MandatController(MandatService mandatService) {
        this.mandatService = mandatService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
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

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<MandatResponse>> getMandat(@PathVariable UUID id) {

        log.info("Consultation mandat id={}", id);

        MandatResponse mandat = mandatService.getMandatById(id);

        log.info("Mandat récupéré id={} numéro={}", mandat.id(), mandat.numMandat());
        return ResponseEntity.ok(ApiRetour.success("Détail du mandat", mandat));
    }

    @PatchMapping("/{id}/resilier")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_AGENT')")
    public ResponseEntity<ApiRetour<MandatResponse>> resilierMandat(@PathVariable UUID id) {
        log.info("Demande résiliation mandat id={}", id);
        MandatResponse mandat = mandatService.resilierMandat(id);
        log.info("Mandat résilié id={} numéro={}", mandat.id(), mandat.numMandat());

        return ResponseEntity.ok(ApiRetour.success("Mandat résilié avec succès", mandat));
    }
}
