package com.example.gestionimmobilier.service.proprietaire;

import com.example.gestionimmobilier.dto.user.CreateProprietaireRequest;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.dto.user.UpdateProprietaireRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.KeycloakAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProprietaireService {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    public ProprietaireService(UtilisateurRepository utilisateurRepository,
                               KeycloakAdminService keycloakAdminService,
                               UserMapper userMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userMapper = userMapper;
    }

    @Transactional
    public ProprietaireResponse createProprietaire(CreateProprietaireRequest request) {
        String keycloakUserId = keycloakAdminService.createUserInKeycloak(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                List.of(Role.ROLE_PROPRIETAIRE)
        );

        keycloakAdminService.syncUserByKeycloakId(keycloakUserId);

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        if (utilisateur instanceof Proprietaire proprietaire) {
            if (request.rib() != null && !request.rib().isBlank()) {
                proprietaire.setRib(request.rib());
            }
            if (request.adresseContact() != null && !request.adresseContact().isBlank()) {
                proprietaire.setAdresseContact(request.adresseContact());
            }
            utilisateurRepository.save(proprietaire);
            return userMapper.toProprietaireResponse(proprietaire);
        }

        return userMapper.toProprietaireResponse(
                Proprietaire.builder()
                        .id(utilisateur.getId())
                        .keycloakId(utilisateur.getKeycloakId())
                        .username(utilisateur.getUsername())
                        .email(utilisateur.getEmail())
                        .firstName(utilisateur.getFirstName())
                        .lastName(utilisateur.getLastName())
                        .roles(utilisateur.getRoles())
                        .emailVerified(utilisateur.isEmailVerified())
                        .build()
        );
    }

    @Transactional
    public ProprietaireResponse updateProprietaire(UUID id, UpdateProprietaireRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE));

        if (!(utilisateur instanceof Proprietaire proprietaire)) {
            throw new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE);
        }

        if (request.rib() != null && !request.rib().isBlank()) {
            proprietaire.setRib(request.rib());
        }
        if (request.adresseContact() != null && !request.adresseContact().isBlank()) {
            proprietaire.setAdresseContact(request.adresseContact());
        }

        utilisateurRepository.save(proprietaire);
        return userMapper.toProprietaireResponse(proprietaire);
    }
}


