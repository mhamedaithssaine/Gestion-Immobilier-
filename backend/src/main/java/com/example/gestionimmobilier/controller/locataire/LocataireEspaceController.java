package com.example.gestionimmobilier.controller.locataire;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.CreateDemandeLocationRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.service.locataire.LocataireEspaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locataire")
@PreAuthorize("hasAnyAuthority('ROLE_CLIENT', 'ROLE_ADMIN')")
public class LocataireEspaceController {

    private final LocataireEspaceService service;

    public LocataireEspaceController(LocataireEspaceService service) {
        this.service = service;
    }

    @GetMapping("/biens")
    public ResponseEntity<ApiRetour<Page<BienResponse>>> listerBiensDisponibles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) BigDecimal minPrix,
            @RequestParam(required = false) BigDecimal maxPrix,
            @RequestParam(required = false) Double minSurface,
            @RequestParam(required = false) Double maxSurface,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BienResponse> data = service.searchBiensDisponibles(
                q, ville, minPrix, maxPrix, minSurface, maxSurface, types, pageable
        );
        return ResponseEntity.ok(ApiRetour.success("Liste des biens disponibles", data));
    }

    @GetMapping("/biens/{id}")
    public ResponseEntity<ApiRetour<BienResponse>> getBien(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiRetour.success("Détail du bien", service.getBienDisponibleById(id)));
    }

    @PostMapping("/biens/{id}/demande-location")
    public ResponseEntity<ApiRetour<ContratResponse>> demanderLocation(
            @PathVariable UUID id,
            @RequestBody @Valid CreateDemandeLocationRequest request) {
        String keycloakId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        ContratResponse contrat = service.creerDemandeLocation(id, keycloakId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Demande de location envoyée avec succès", contrat));
    }

    @GetMapping("/mes-demandes")
    public ResponseEntity<ApiRetour<List<ContratResponse>>> listerMesDemandes() {
        String keycloakId = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
        List<ContratResponse> data = service.listerMesDemandes(keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Liste de vos demandes et contrats", data));
    }
}

