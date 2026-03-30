package com.example.gestionimmobilier.models.enums;

public enum StatutBail {
    /** Demande sans mandat actif sur le bien : validation par le propriétaire. */
    EN_ATTENTE,
    /** Demande avec mandat actif : validation uniquement par l’agent mandaté. */
    EN_ATTENTE_VALIDATION_AGENT,
    ACTIF,
    /** Demande refusée par l’agent (bien reste disponible). */
    REFUSE,
    RESILIE,
    TERMINE
}
