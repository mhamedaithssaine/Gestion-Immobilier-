package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    /**
     * Inscriptions publiques (locataire / propriétaire) en attente d’activation admin.
     * Exclut les agents créés via enregistrement d’agence ({@code enabled=false} mais type Agent).
     */
    @Query("""
            SELECT u FROM Utilisateur u
            WHERE u.enabled = false
              AND (TYPE(u) = Client OR TYPE(u) = Proprietaire)
            ORDER BY u.createdAt DESC
            """)
    List<Utilisateur> findPublicRegistrationPendingActivation();

    @Query("""
            SELECT COUNT(u) FROM Utilisateur u
            WHERE u.enabled = false
              AND (TYPE(u) = Client OR TYPE(u) = Proprietaire)
            """)
    long countPublicRegistrationPendingActivation();
}
