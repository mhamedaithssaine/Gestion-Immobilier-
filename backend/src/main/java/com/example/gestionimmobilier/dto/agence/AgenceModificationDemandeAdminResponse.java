package com.example.gestionimmobilier.dto.agence;

import com.example.gestionimmobilier.models.enums.StatutDemandeModificationAgence;
import com.example.gestionimmobilier.models.enums.TypeDemandeModificationAgence;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgenceModificationDemandeAdminResponse(
        UUID id,
        UUID agenceId,
        String agenceNom,
        TypeDemandeModificationAgence type,
        StatutDemandeModificationAgence statut,
        String nomPropose,
        String emailPropose,
        String telephonePropose,
        String adressePropose,
        String villePropose,
        LocalDateTime createdAt,
        String demandeurKeycloakId
) {}
