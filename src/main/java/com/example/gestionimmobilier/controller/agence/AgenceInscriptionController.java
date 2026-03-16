package com.example.gestionimmobilier.controller.agence;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.dto.agence.CreateAgenceRequest;
import com.example.gestionimmobilier.service.agence.AgenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/agences")
public class AgenceInscriptionController {

    private final AgenceService agenceService;

    public AgenceInscriptionController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    @PostMapping("/inscription")
    public ResponseEntity<ApiRetour<AgenceResponse>> inscrire(
            @RequestBody @Valid CreateAgenceRequest request) {
        AgenceResponse agence = agenceService.inscrire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Inscription envoyée. Votre agence sera examinée par l'administrateur.", agence));
    }
}
