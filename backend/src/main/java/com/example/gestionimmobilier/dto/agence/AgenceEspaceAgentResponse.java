package com.example.gestionimmobilier.dto.agence;

public record AgenceEspaceAgentResponse(
        AgenceResponse agence,
        AgenceDemandeEnAttenteResponse demandeEnAttente
) {}
