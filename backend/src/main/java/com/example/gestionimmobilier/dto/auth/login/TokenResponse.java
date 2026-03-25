package com.example.gestionimmobilier.dto.auth.login;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long refreshExpiresIn,
        String scope
) {
}
