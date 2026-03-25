package com.example.gestionimmobilier.controller.bienImmobilier;

import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.immobilier.CreateBienRequest;
import com.example.gestionimmobilier.service.bienImmobilier.BienService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proprietaire/biens")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PROPRIETAIRE')")
public class BienImmobilierController {

    private final BienService bienService;

    public BienImmobilierController(BienService bienService) {
        this.bienService = bienService;
    }

    @GetMapping
    public ResponseEntity<ApiRetour<List<BienResponse>>> listerMesBiens() {
        String keycloakId = getCurrentKeycloakId();
        List<BienResponse> biens = bienService.listerBiens(keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Liste des biens", biens));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiRetour<BienResponse>> getBienById(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        BienResponse bien = bienService.getBienById(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.success("Détail du bien", bien));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<ApiRetour<BienResponse>> creerBienJson(@RequestBody @Valid CreateBienRequest data) {
    String keycloakId = getCurrentKeycloakId();
    BienResponse bien = bienService.creerBien(keycloakId, data, new MultipartFile[0]);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiRetour.success("Bien immobilier créé avec succès", bien));
}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiRetour<BienResponse>> creerBienMultipart(
            @RequestPart("data") String data,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        CreateBienRequest request =
                mapper.readValue(data, CreateBienRequest.class);

        String keycloakId = getCurrentKeycloakId();

        BienResponse bien = bienService.creerBien(keycloakId, request, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Bien immobilier créé avec succès", bien));
    }


    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiRetour<BienResponse>> modifierBien(
            @PathVariable UUID id,
            @RequestBody @Valid CreateBienRequest data) {
        String keycloakId = getCurrentKeycloakId();
        BienResponse bien = bienService.modifierBien(id, keycloakId, data, new MultipartFile[0]);
        return ResponseEntity.ok(ApiRetour.success("Bien immobilier modifié avec succès", bien));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiRetour<Void>> supprimerBien(@PathVariable UUID id) {
        String keycloakId = getCurrentKeycloakId();
        bienService.supprimerBien(id, keycloakId);
        return ResponseEntity.ok(ApiRetour.<Void>success("Bien immobilier supprimé avec succès"));
    }

    private String getCurrentKeycloakId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }
}
