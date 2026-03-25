package com.example.gestionimmobilier.dto.immobilier;

import java.util.UUID;

public record AdresseResponse(
        UUID id,
        String rue,
        String ville,
        String codePostal,
        String pays
) {}
