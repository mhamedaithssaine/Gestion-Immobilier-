package com.example.gestionimmobilier.dto.dashboard;

import java.math.BigDecimal;

public record RevenusMensuelsResponse(int annee, int mois, BigDecimal revenusMensuels) {}
