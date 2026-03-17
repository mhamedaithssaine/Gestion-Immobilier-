package com.example.gestionimmobilier.dto.dashboard;

import java.util.UUID;

public record MandatsGestionParAgentResponse(
        UUID agentId,
        String nomAgent,
        String agenceNom,
        long totalMandats,
        long mandatsActifs,
        long mandatsResilies,
        long mandatsTermines,
        long mandatsEnAttente
) {}
