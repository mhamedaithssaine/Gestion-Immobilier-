package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);
}
