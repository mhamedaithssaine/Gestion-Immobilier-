package com.example.gestionimmobilier.controller;

import com.example.gestionimmobilier.dto.api.reponse.ApiRetour;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.service.KeycloakAdminService;
import com.example.gestionimmobilier.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class KeycloakUserController {

    private final UserService userService;

    public KeycloakUserController(KeycloakAdminService keycloakAdminService, UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiRetour<List<UtilisateurResponse>>> getUsers(
            @RequestParam(defaultValue = "true") boolean sync) {
        List<UtilisateurResponse> users = userService.getUsersFromDatabase(sync);
        return ResponseEntity.ok(ApiRetour.success("Liste des utilisateurs", users));
    }
}