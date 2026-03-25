package com.example.gestionimmobilier.dto.agence;

import com.example.gestionimmobilier.models.enums.StatutAgence;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgenceResponse(
        UUID id,
        String nom,
        String email,
        String telephone,
        String adresse,
        String ville,
        StatutAgence statut,
        LocalDateTime createdAt
) {}
