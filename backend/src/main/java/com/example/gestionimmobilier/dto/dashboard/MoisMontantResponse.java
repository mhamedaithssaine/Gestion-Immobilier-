package com.example.gestionimmobilier.dto.dashboard;

import java.math.BigDecimal;

/** Point pour une série temporelle (revenus, etc.). */
public record MoisMontantResponse(int annee, int mois, BigDecimal montant) {}
