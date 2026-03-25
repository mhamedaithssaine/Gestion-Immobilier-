package com.example.gestionimmobilier.dto.dashboard;

public record BiensLouesVsLibresResponse(
        long disponibles,
        long loues,
        long vendus,
        long sousCompromis
) {}
