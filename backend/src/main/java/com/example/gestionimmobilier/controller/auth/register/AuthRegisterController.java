package com.example.gestionimmobilier.controller.auth.register;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.auth.register.RegisterRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.service.auth.register.AuthRegisterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth/register")
public class AuthRegisterController {

    private final AuthRegisterService authRegisterService;

    public AuthRegisterController(AuthRegisterService authRegisterService) {
        this.authRegisterService = authRegisterService;
    }

    @PostMapping
    public ResponseEntity<ApiRetour<UtilisateurResponse>> register(@RequestBody @Valid RegisterRequest request) {
        UtilisateurResponse created = authRegisterService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Inscription réussie. Compte en attente d'activation par un administrateur.", created));
    }
}
