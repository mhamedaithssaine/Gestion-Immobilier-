package com.example.gestionimmobilier.controller.user;

import com.example.gestionimmobilier.dto.api.response.ApiRetour;
import com.example.gestionimmobilier.dto.user.AssignRolesRequest;
import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UpdateUserEnabledRequest;
import com.example.gestionimmobilier.dto.user.UpdateUserRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.service.user.KeycloakAdminService;
import com.example.gestionimmobilier.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @GetMapping("/users/paged")
    public ResponseEntity<ApiRetour<Page<UtilisateurResponse>>> getUsersPaged(
            @RequestParam(defaultValue = "true") boolean sync,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UtilisateurResponse> data = sync
                ? userService.getUsersFromDatabase(true, pageable)
                : userService.getUsersFromDatabasePaged(pageable);
        return ResponseEntity.ok(ApiRetour.success("Liste des utilisateurs paginée", data));
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

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiRetour<UtilisateurResponse>> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        UtilisateurResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiRetour.success("Utilisateur modifié avec succès", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiRetour<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiRetour.success("Utilisateur supprimé avec succès", null));
    }

}