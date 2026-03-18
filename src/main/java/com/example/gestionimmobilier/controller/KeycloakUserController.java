package com.example.gestionimmobilier.controller;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.user.AssignRolesRequest;
import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UpdateUserEnabledRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.service.KeycloakAdminService;
import com.example.gestionimmobilier.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class KeycloakUserController {

    private final UserService userService;

    public KeycloakUserController(KeycloakAdminService keycloakAdminService, UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiRetour<List<UtilisateurResponse>>> getUsers(
            @RequestParam(defaultValue = "true") boolean sync) {
        List<UtilisateurResponse> users = userService.getUsersFromDatabase(sync);
        return ResponseEntity.ok(ApiRetour.success("Liste des utilisateurs", users));
    }


    @PutMapping("/users/{id}/roles")
    public ResponseEntity<ApiRetour<UtilisateurResponse>> assignRoles(
            @PathVariable UUID id,
            @RequestBody @Valid AssignRolesRequest request) {
        UtilisateurResponse user = userService.assignRoles(id, request.roles());
        return ResponseEntity.ok(ApiRetour.success("Rôles attribués avec succès", user));
    }

    @PutMapping("/users/{id}/enabled")
    public ResponseEntity<ApiRetour<UtilisateurResponse>> updateUserEnabled(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserEnabledRequest request) {
        UtilisateurResponse user = userService.updateUserEnabled(id, request.enabled());
        return ResponseEntity.ok(ApiRetour.success(
                request.enabled() ? "Utilisateur activé avec succès" : "Utilisateur désactivé avec succès",
                user));
    }


    @PostMapping("/users")
    public ResponseEntity<ApiRetour<UtilisateurResponse>> createUser(
            @RequestBody @Valid CreateUserRequest request) {
        UtilisateurResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiRetour.success("Utilisateur créé avec succès", user));
    }

}