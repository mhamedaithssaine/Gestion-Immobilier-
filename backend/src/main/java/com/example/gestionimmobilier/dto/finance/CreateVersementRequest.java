package com.example.gestionimmobilier.dto.finance;

import com.example.gestionimmobilier.models.enums.ModeVersement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateVersementRequest(
        @NotNull(message = "L'identifiant du bail est requis") 
        UUID bailId,
        @NotNull(message = "Le montant est requis")
        @DecimalMin(value = "0.01", message = "Le montant doit être strictement positif") 
        BigDecimal montant,
        @NotNull(message = "Le mode de versement est requis") 
        ModeVersement mode,
        @Size(max = 255) 
        String referencePaiement,
        LocalDateTime dateVersement
) {}
