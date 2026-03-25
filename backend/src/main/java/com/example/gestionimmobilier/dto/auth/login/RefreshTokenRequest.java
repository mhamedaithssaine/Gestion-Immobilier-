package com.example.gestionimmobilier.dto.auth.login;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est requis")
        String refreshToken
) {
}
