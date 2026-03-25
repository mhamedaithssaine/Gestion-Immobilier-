package com.example.gestionimmobilier.service.auth.login;

import com.example.gestionimmobilier.dto.auth.login.TokenResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.UnauthorizedException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.service.user.KeycloakAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Service
public class AuthLoginService {

    private final KeycloakAdminService keycloakAdminService;

    @Value("${keycloak.auth.client-id:${keycloak.admin.client-id}}")
    private String authClientId;

    public AuthLoginService(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    public TokenResponse login(String username, String password) {
        try {
            Map<String, Object> tokenBody = keycloakAdminService.getUserTokenByPassword(authClientId, username, password);
            return mapTokenResponse(tokenBody);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UnauthorizedException(ErrorMessages.IDENTIFIANTS_INVALIDES);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new UnauthorizedException(ErrorMessages.IDENTIFIANTS_INVALIDES);
        }
    }

    public TokenResponse refresh(String refreshToken) {
        try {
            Map<String, Object> tokenBody = keycloakAdminService.refreshUserToken(authClientId, refreshToken);
            return mapTokenResponse(tokenBody);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UnauthorizedException(ErrorMessages.IDENTIFIANTS_INVALIDES);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new UnauthorizedException(ErrorMessages.IDENTIFIANTS_INVALIDES);
        }
    }

    private TokenResponse mapTokenResponse(Map<String, Object> body) {
        if (body == null) {
            throw new ValidationException(ErrorMessages.DONNEES_INVALIDES);
        }
        String accessToken = asString(body.get("access_token"));
        String refreshToken = asString(body.get("refresh_token"));
        if (accessToken == null || refreshToken == null) {
            throw new ValidationException(ErrorMessages.DONNEES_INVALIDES);
        }
        return new TokenResponse(
                accessToken,
                refreshToken,
                asString(body.get("token_type")),
                asLong(body.get("expires_in")),
                asLong(body.get("refresh_expires_in")),
                asString(body.get("scope"))
        );
    }

    private String asString(Object value) {
        return value instanceof String s ? s : null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
