package com.example.gestionimmobilier.service;

import com.example.gestionimmobilier.dto.user.UtilisateurResponse;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}