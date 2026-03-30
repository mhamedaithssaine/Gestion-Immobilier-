package com.example.gestionimmobilier.dto.agence;

import com.example.gestionimmobilier.models.enums.TypeDemandeModificationAgence;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgenceDemandeEnAttenteResponse(
        UUID id,
        TypeDemandeModificationAgence type,
        String nomPropose,
        String emailPropose,
        String telephonePropose,
        String adressePropose,
        String villePropose,
        LocalDateTime demandeLe
) {}
