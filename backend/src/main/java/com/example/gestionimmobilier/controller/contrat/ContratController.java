package com.example.gestionimmobilier.controller.contrat;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.CreateContratRequest;
import com.example.gestionimmobilier.dto.contrat.UpdateContratRequest;
import com.example.gestionimmobilier.dto.finance.ResteAPayerResponse;
import com.example.gestionimmobilier.service.contrat.ContratService;
import com.example.gestionimmobilier.service.finance.VersementService;
import com.example.gestionimmobilier.models.enums.StatutBail;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/contrats")
public class ContratController {

    private final ContratService contratService;
    private final VersementService versementService;

    public ContratController(ContratService contratService, VersementService versementService) {
        this.contratService = contratService;
        this.versementService = versementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT')")
    public ResponseEntity<ApiRetour<ContratResponse>> creerContrat(
            @RequestBody @Valid CreateContratRequest request) {
        ContratResponse contrat = contratService.creerContrat(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Contrat de location créé avec succès", contrat));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPRIETAIRE','ROLE_AGENT')")
    public ResponseEntity<ApiRetour<List<ContratResponse>>> listerContrats(
            @RequestParam(required = false) StatutBail statut,
            @RequestParam(required = false) UUID locataireId) {
        List<ContratResponse> contrats = locataireId != null
                ? contratService.listerContratsParLocataire(locataireId)
                : contratService.listerContrats(statut);
        return ResponseEntity.ok(ApiRetour.success("Liste des contrats", contrats));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT', 'ROLE_PROPRIETAIRE','ROLE_AGENT')")
    public ResponseEntity<ApiRetour<ContratResponse>> getContrat(@PathVariable UUID id) {
        ContratResponse contrat = contratService.getContratById(id);
        return ResponseEntity.ok(ApiRetour.success("Détail du contrat", contrat));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPRIETAIRE', 'ROLE_AGENT')")
    public ResponseEntity<ApiRetour<ContratResponse>> modifierContrat(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateContratRequest request) {
        ContratResponse contrat = contratService.modifierContrat(id, request);
        return ResponseEntity.ok(ApiRetour.success("Contrat modifié avec succès", contrat));
    }

    @PatchMapping("/{id}/resilier")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPRIETAIRE', 'ROLE_AGENT')")
    public ResponseEntity<ApiRetour<ContratResponse>> resilierContrat(@PathVariable UUID id) {
        ContratResponse contrat = contratService.resilierContrat(id);
        return ResponseEntity.ok(ApiRetour.success("Contrat résilié avec succès", contrat));
    }

    @GetMapping("/{id}/reste-a-payer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT','ROLE_PROPRIETAIRE','ROLE_AGENT')")
    public ResponseEntity<ApiRetour<ResteAPayerResponse>> getResteAPayer(
            @PathVariable UUID id,
            @RequestParam int annee,
            @RequestParam int mois) {
        ResteAPayerResponse reste = versementService.getResteAPayer(id, annee, mois);
        return ResponseEntity.ok(ApiRetour.success("Reste à payer", reste));
    }
}

