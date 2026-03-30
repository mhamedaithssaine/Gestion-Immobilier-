package com.example.gestionimmobilier.dto.agence;

import jakarta.validation.constraints.Size;

public record ReviewAgenceDemandeRequest(
        @Size(max = 500)
        String commentaire
) {}
