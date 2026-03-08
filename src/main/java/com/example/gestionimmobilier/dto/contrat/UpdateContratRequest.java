package com.example.gestionimmobilier.dto.contrat;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateContratRequest(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal loyerHC,
        BigDecimal charges,
        LocalDate dateSignature,
        String documentUrl
) {}
