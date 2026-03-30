package com.example.gestionimmobilier.controller.agence;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.agence.AgenceModificationDemandeAdminResponse;
import com.example.gestionimmobilier.dto.agence.ReviewAgenceDemandeRequest;
import com.example.gestionimmobilier.service.agence.AgenceModificationDemandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/agences/modifications")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AgenceModificationAdminController {

    private final AgenceModificationDemandeService modificationDemandeService;

    public AgenceModificationAdminController(AgenceModificationDemandeService modificationDemandeService) {
        this.modificationDemandeService = modificationDemandeService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiRetour<List<AgenceModificationDemandeAdminResponse>>> listerEnAttente() {
        List<AgenceModificationDemandeAdminResponse> list = modificationDemandeService.listerDemandesEnAttenteAdmin();
        return ResponseEntity.ok(ApiRetour.success("Demandes en attente", list));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiRetour<AgenceModificationDemandeAdminResponse>> approuver(@PathVariable UUID id) {
        AgenceModificationDemandeAdminResponse data = modificationDemandeService.approuverDemandeAdmin(id);
        return ResponseEntity.ok(ApiRetour.success("Demande approuvée", data));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiRetour<AgenceModificationDemandeAdminResponse>> rejeter(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ReviewAgenceDemandeRequest request) {
        String c = request != null ? request.commentaire() : null;
        AgenceModificationDemandeAdminResponse data = modificationDemandeService.rejeterDemandeAdmin(id, c);
        return ResponseEntity.ok(ApiRetour.success("Demande rejetée", data));
    }
}
