package com.example.gestionimmobilier.dto.dashboard;

import java.util.UUID;

public record MandatsGestionStatistiqueResponse(
        UUID agenceId,
        String nomAgence,
        long totalMandats,
        long mandatsActifs,
        long mandatsResilies,
        long mandatsTermines,
        long mandatsEnAttente
) {}
