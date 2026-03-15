package com.example.gestionimmobilier.mapper;

import com.example.gestionimmobilier.dto.keycloack.KeycloakUserResponse;
import com.example.gestionimmobilier.models.entity.user.*;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutDossier;
import org.mapstruct.Mapper;


import java.util.List;

@Mapper(componentModel = "spring")
public interface KeycloakUserMapper {


    default Utilisateur toClient(KeycloakUserResponse response, List<Role> roles) {
        Client c = Client.builder()
                .keycloakId(response.id())
                .username(response.username())
                .email(response.email() != null ? response.email() : response.username() + "@keycloak.local")
                .firstName(response.firstName())
                .lastName(response.lastName())
                .roles(roles)
                .emailVerified(response.emailVerified())
                .enabled(response.enabled())
                .build();
        return c;
    }

    default Utilisateur toProprietaire(KeycloakUserResponse response, List<Role> roles) {
        return Proprietaire.builder()
                .keycloakId(response.id())
                .username(response.username())
                .email(response.email() != null ? response.email() : response.username() + "@keycloak.local")
                .firstName(response.firstName())
                .lastName(response.lastName())
                .roles(roles)
                .emailVerified(response.emailVerified())
                .enabled(response.enabled())
                .build();
    }

    default Utilisateur toAgent(KeycloakUserResponse response, List<Role> roles) {
        return Agent.builder()
                .keycloakId(response.id())
                .username(response.username())
                .email(response.email() != null ? response.email() : response.username() + "@keycloak.local")
                .firstName(response.firstName())
                .lastName(response.lastName())
                .roles(roles)
                .emailVerified(response.emailVerified())
                .enabled(response.enabled())
                .build();
    }

    default Utilisateur toAdmin(KeycloakUserResponse response, List<Role> roles) {
        return Admin.builder()
                .keycloakId(response.id())
                .username(response.username())
                .email(response.email() != null ? response.email() : response.username() + "@keycloak.local")
                .firstName(response.firstName())
                .lastName(response.lastName())
                .roles(roles)
                .emailVerified(response.emailVerified())
                .enabled(response.enabled())
                .build();
    }
}