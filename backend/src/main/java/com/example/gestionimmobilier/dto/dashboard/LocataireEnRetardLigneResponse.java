package com.example.gestionimmobilier.dto.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record LocataireEnRetardLigneResponse(
        UUID contratId,
        String numContrat,
        String locataireNom,
        String bienReference,
        int annee,
        int mois,
        BigDecimal resteAPayer
) {}
