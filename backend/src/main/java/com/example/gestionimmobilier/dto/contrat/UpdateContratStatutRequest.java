package com.example.gestionimmobilier.dto.contrat;

import com.example.gestionimmobilier.models.enums.StatutBail;
import jakarta.validation.constraints.NotNull;

public record UpdateContratStatutRequest(
        @NotNull(message = "Le statut est requis")
        StatutBail statut
) {}

