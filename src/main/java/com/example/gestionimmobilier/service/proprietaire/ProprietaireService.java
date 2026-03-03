package com.example.gestionimmobilier.service.proprietaire;

import com.example.gestionimmobilier.dto.user.CreateProprietaireRequest;
import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
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
    public UtilisateurResponse createProprietaire(CreateProprietaireRequest request) {
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
        }

        return userMapper.toResponse(utilisateur);
    }
}
