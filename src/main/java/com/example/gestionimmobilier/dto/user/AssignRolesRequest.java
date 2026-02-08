package com.example.gestionimmobilier.dto.user;

import com.example.gestionimmobilier.models.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignRolesRequest(
        @NotNull(message = "La liste des rôles ne peut pas être null")
        @NotEmpty(message = "Au moins un rôle doit être attribué")
        List<Role> roles
) {}