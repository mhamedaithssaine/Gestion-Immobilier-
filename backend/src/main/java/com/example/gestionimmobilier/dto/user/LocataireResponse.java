package com.example.gestionimmobilier.dto.user;

import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutDossier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LocataireResponse(
        UUID id,
        String keycloakId,
        String username,
        String email,
        String firstName,
        String lastName,
        List<Role> roles,
        String type,
        boolean emailVerified,
        BigDecimal budgetMax,
        StatutDossier statutDossier
) {}
