package com.example.gestionimmobilier.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data transfer object for Thymeleaf quittance PDF template.
 */
public record QuittancePdfData(
        String referenceQuittance,
        int mois,
        int annee,
        String moisLibelle,
        String adresseBien,
        String proprietaireNom,
        String proprietaireAdresse,
        String locataireNom,
        String locataireAdresse,
        String numContrat,
        BigDecimal loyerHC,
        BigDecimal charges,
        BigDecimal montantTotal,
        LocalDateTime dateVersement,
        String modePaiement,
        String referencePaiement,
        LocalDateTime dateGeneration
) {
    public String getDateVersementFormatted() {
        return dateVersement == null ? "" : dateVersement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getDateGenerationFormatted() {
        return dateGeneration == null ? "" : dateGeneration.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
