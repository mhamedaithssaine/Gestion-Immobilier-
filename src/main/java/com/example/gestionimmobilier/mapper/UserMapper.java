package com.example.gestionimmobilier.mapper;

import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "type", expression = "java(getType(utilisateur))")
    @Mapping(target = "emailVerified", expression = "java(utilisateur.isEmailVerified())")
    UtilisateurResponse toResponse(Utilisateur utilisateur);

    @Mapping(target = "type", expression = "java(getType(proprietaire))")
    @Mapping(target = "emailVerified", expression = "java(proprietaire.isEmailVerified())")
    ProprietaireResponse toProprietaireResponse(Proprietaire proprietaire);

    @Mapping(target = "type", expression = "java(getType(client))")
    @Mapping(target = "emailVerified", expression = "java(client.isEmailVerified())")
    LocataireResponse toLocataireResponse(Client client);

    default String getType(Utilisateur u) {
        List<Role> roles = u.getRoles();
        if (roles == null) return "UTILISATEUR";
        if (roles.contains(Role.ROLE_ADMIN)) return "ADMIN";
        if (roles.contains(Role.ROLE_AGENT)) return "AGENT";
        if (roles.contains(Role.ROLE_CLIENT)) return "CLIENT";
        if (roles.contains(Role.ROLE_PROPRIETAIRE)) return "PROPRIETAIRE";
        return "UTILISATEUR";
    }
}
