package com.example.gestionimmobilier.dto.user;

import com.example.gestionimmobilier.models.enums.StatutDossier;

import java.math.BigDecimal;

public record UpdateLocataireRequest(
        BigDecimal budgetMax,
        StatutDossier statutDossier
) {}
