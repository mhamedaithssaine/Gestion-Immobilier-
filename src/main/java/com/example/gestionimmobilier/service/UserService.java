package com.example.gestionimmobilier.service;

import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    public UserService(UtilisateurRepository utilisateurRepository,
                       KeycloakAdminService keycloakAdminService,
                       UserMapper userMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userMapper = userMapper;
    }

    @Transactional
    public List<UtilisateurResponse> getUsersFromDatabase(boolean syncFromKeycloak) {
        if (syncFromKeycloak) {
            keycloakAdminService.syncUsersToDatabase();
        }
        return utilisateurRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UtilisateurResponse assignRoles(UUID userId, List<Role> roles) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.assignRolesToUser(utilisateur.getKeycloakId(), roles);

        utilisateur.setRoles(roles);
        utilisateurRepository.save(utilisateur);

        return userMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse updateUserEnabled(UUID userId, boolean enabled) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.setUserEnabled(utilisateur.getKeycloakId(), enabled);
        utilisateur.setEnabled(enabled);
        utilisateurRepository.save(utilisateur);

        return userMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse createUser(CreateUserRequest request) {
        if (request.roles() == null || request.roles().isEmpty()) {
            throw new ValidationException(ErrorMessages.AUCUN_ROLE_VALIDE);
        }

        String keycloakUserId = keycloakAdminService.createUserInKeycloak(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.roles()
        );

        keycloakAdminService.syncUserByKeycloakId(keycloakUserId);

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        return userMapper.toResponse(utilisateur);
    }
}