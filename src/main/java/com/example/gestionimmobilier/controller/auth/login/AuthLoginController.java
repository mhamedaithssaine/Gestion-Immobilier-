package com.example.gestionimmobilier.controller.auth.login;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.auth.login.LoginRequest;
import com.example.gestionimmobilier.dto.auth.login.RefreshTokenRequest;
import com.example.gestionimmobilier.dto.auth.login.TokenResponse;
import com.example.gestionimmobilier.service.auth.login.AuthLoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth/login")
public class AuthLoginController {

    private final AuthLoginService authLoginService;

    public AuthLoginController(AuthLoginService authLoginService) {
        this.authLoginService = authLoginService;
    }

    @PostMapping
    public ResponseEntity<ApiRetour<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse token = authLoginService.login(request.username(), request.password());
        return ResponseEntity.ok(ApiRetour.success("Connexion réussie", token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiRetour<TokenResponse>> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenResponse token = authLoginService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiRetour.success("Token rafraîchi", token));
    }
}
