package com.example.gestionimmobilier.controller;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.keycloack.KeycloakUserResponse;
import com.example.gestionimmobilier.service.KeycloakAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class KeycloakUserController {

    private final KeycloakAdminService keycloakAdminService;

    public KeycloakUserController(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<KeycloakUserResponse>>> getUsers() {
        List<KeycloakUserResponse> users = keycloakAdminService.getUsers();
        return ResponseEntity.ok(ApiRetour.success("Liste des utilisateurs Keycloak", users));
    }
}