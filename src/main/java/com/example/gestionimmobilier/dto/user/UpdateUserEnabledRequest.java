package com.example.gestionimmobilier.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserEnabledRequest(
        @NotNull(message = "Le champ 'enabled' est requis")
        Boolean enabled
) {}