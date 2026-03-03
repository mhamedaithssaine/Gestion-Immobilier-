package com.example.gestionimmobilier.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateProprietaireRequest(
        @Size(max = 50)
        String rib,

        @Size(max = 500)
        String adresseContact
) {}

