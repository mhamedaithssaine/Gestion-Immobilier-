package com.example.gestionimmobilier.service.user;

import com.example.gestionimmobilier.dto.user.CreateUserRequest;
import com.example.gestionimmobilier.dto.user.UpdateUserRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
        log.info("Listing users (syncFromKeycloak={})", syncFromKeycloak);
        if (syncFromKeycloak) {
            keycloakAdminService.syncUsersToDatabase();
        }
        return utilisateurRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    
    @Transactional(readOnly = true)
    public Page<UtilisateurResponse> getUsersFromDatabasePaged(Pageable pageable) {
        return fetchUsersPage(pageable);
    }

   
    @Transactional
    public Page<UtilisateurResponse> getUsersFromDatabase(boolean syncFromKeycloak, Pageable pageable) {
        log.info("Listing users paged (syncFromKeycloak={}) pageable={}", syncFromKeycloak, pageable);
        if (syncFromKeycloak) {
            keycloakAdminService.syncUsersToDatabase();
        }
        return fetchUsersPage(pageable);
    }

    private Page<UtilisateurResponse> fetchUsersPage(Pageable pageable) {
        return utilisateurRepository
                .findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional
    public UtilisateurResponse assignRoles(UUID userId, List<Role> roles) {
        log.info("Assign roles to user {} roles={}", userId, roles);
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.assignRolesToUser(utilisateur.getKeycloakId(), roles);

        utilisateur.setRoles(roles);
        utilisateurRepository.save(utilisateur);

        return userMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse updateUserEnabled(UUID userId, boolean enabled) {
        log.info("Update user enabled {} enabled={}", userId, enabled);
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.setUserEnabled(utilisateur.getKeycloakId(), enabled);
        utilisateur.setEnabled(enabled);
        utilisateurRepository.save(utilisateur);

        return userMapper.toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse createUser(CreateUserRequest request) {
        return createUser(request, true);
    }

    @Transactional
    public UtilisateurResponse createUser(CreateUserRequest request, boolean enabled) {
        log.info("Create user username={} email={} roles={}", request.username(), request.email(), request.roles());
        if (request.roles() == null || request.roles().isEmpty()) {
            throw new ValidationException(ErrorMessages.AUCUN_ROLE_VALIDE);
        }

        String keycloakUserId = keycloakAdminService.createUserInKeycloak(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.roles(),
                enabled
        );

        keycloakAdminService.syncUserByKeycloakId(keycloakUserId);

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        return userMapper.toResponse(utilisateur);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Delete user {}", userId);
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.deleteUserInKeycloak(utilisateur.getKeycloakId());
        utilisateurRepository.delete(utilisateur);
    }

    @Transactional
    public UtilisateurResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Update user {} username={} email={}", userId, request.username(), request.email());
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        keycloakAdminService.updateUserProfile(
                utilisateur.getKeycloakId(),
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName()
        );

        keycloakAdminService.syncUserByKeycloakId(utilisateur.getKeycloakId());

        Utilisateur updated = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        return userMapper.toResponse(updated);
    }
}