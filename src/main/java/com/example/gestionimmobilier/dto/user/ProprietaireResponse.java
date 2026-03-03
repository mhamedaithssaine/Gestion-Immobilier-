package com.example.gestionimmobilier.dto.user;

import com.example.gestionimmobilier.models.enums.Role;

import java.util.List;
import java.util.UUID;

public record ProprietaireResponse(
        UUID id,
        String keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        List<Role> roles,
        String type,
        boolean emailVerified,
        String rib,
        String adresseContact
) {}

