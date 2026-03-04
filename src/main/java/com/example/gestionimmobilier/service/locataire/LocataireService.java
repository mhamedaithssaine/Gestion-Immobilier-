package com.example.gestionimmobilier.service.locataire;

import com.example.gestionimmobilier.dto.user.CreateLocataireRequest;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.UpdateLocataireRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutDossier;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.KeycloakAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LocataireService {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    public LocataireService(UtilisateurRepository utilisateurRepository,
                            KeycloakAdminService keycloakAdminService,
                            UserMapper userMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.userMapper = userMapper;
    }

    @Transactional
    public LocataireResponse createLocataire(CreateLocataireRequest request) {
        String keycloakUserId = keycloakAdminService.createUserInKeycloak(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                List.of(Role.ROLE_CLIENT)
        );

        keycloakAdminService.syncUserByKeycloakId(keycloakUserId);

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        if (utilisateur instanceof Client client) {
            if (request.budgetMax() != null) {
                client.setBudgetMax(request.budgetMax());
            }
            if (request.statutDossier() != null) {
                client.setStatutDossier(request.statutDossier());
            } else {
                client.setStatutDossier(StatutDossier.EN_ATTENTE);
            }
            utilisateurRepository.save(client);
            return userMapper.toLocataireResponse(client);
        }

        throw new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE);
    }

    @Transactional
    public LocataireResponse updateLocataire(UUID id, UpdateLocataireRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE));

        if (!(utilisateur instanceof Client client)) {
            throw new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE);
        }

        if (request.budgetMax() != null) {
            client.setBudgetMax(request.budgetMax());
        }
        if (request.statutDossier() != null) {
            client.setStatutDossier(request.statutDossier());
        }

        utilisateurRepository.save(client);
        return userMapper.toLocataireResponse(client);
    }

    @Transactional
    public void deleteLocataire(UUID id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE));

        if (!(utilisateur instanceof Client client)) {
            throw new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE);
        }

        String keycloakId = client.getKeycloakId();
        utilisateurRepository.delete(client);
        keycloakAdminService.deleteUserInKeycloak(keycloakId);
    }
}
