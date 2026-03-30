package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.enums.StatutBien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BienImmobilierRepository extends JpaRepository<BienImmobilier, UUID> {

    boolean existsByReference(String reference);

    List<BienImmobilier> findByProprietaireOrderByCreatedAtDesc(Proprietaire proprietaire);
    List<BienImmobilier> findByProprietaire_IdOrderByCreatedAtDesc(UUID proprietaireId);

    Optional<BienImmobilier> findByIdAndProprietaire(UUID id, Proprietaire proprietaire);

    List<BienImmobilier> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT DISTINCT m.bien
            FROM MandatDeGestion m
            WHERE m.agent.id = :agentId
            ORDER BY m.bien.createdAt DESC
            """)
    List<BienImmobilier> findDistinctByAgentMandat(@Param("agentId") UUID agentId);

    @Query("""
            SELECT COUNT(m) > 0
            FROM MandatDeGestion m
            WHERE m.bien.id = :bienId
              AND m.agent.id = :agentId
            """)
    boolean existsAgentAccessToBien(@Param("agentId") UUID agentId, @Param("bienId") UUID bienId);

    long countByStatut(StatutBien statut);

    long countByProprietaire_KeycloakId(String keycloakId);

    long countByProprietaire_KeycloakIdAndStatut(String keycloakId, StatutBien statut);

    @Query("SELECT COALESCE(SUM(b.prixBase), 0) FROM BienImmobilier b WHERE b.proprietaire.keycloakId = :kc")
    java.math.BigDecimal sumPrixBasePourProprietaireKeycloak(@Param("kc") String keycloakId);

    @Query("""
            SELECT b
            FROM BienImmobilier b
            WHERE b.statut = com.example.gestionimmobilier.models.enums.StatutBien.DISPONIBLE
              AND (:query IS NULL OR :query = '' OR
                   LOWER(b.reference) LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(b.titre) LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(b.adresse.ville) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:ville IS NULL OR :ville = '' OR LOWER(b.adresse.ville) = LOWER(:ville))
              AND (:minPrix IS NULL OR b.prixBase >= :minPrix)
              AND (:maxPrix IS NULL OR b.prixBase <= :maxPrix)
              AND (:minSurface IS NULL OR b.surface >= :minSurface)
              AND (:maxSurface IS NULL OR b.surface <= :maxSurface)
              AND (
                   :noTypeFilter = true
                   OR (:wantAppartement = true AND TYPE(b) = Appartement)
                   OR (:wantMaison = true AND TYPE(b) = Maison)
              )
            """)
    Page<BienImmobilier> searchDisponibles(
            @Param("query") String query,
            @Param("ville") String ville,
            @Param("minPrix") BigDecimal minPrix,
            @Param("maxPrix") BigDecimal maxPrix,
            @Param("minSurface") Double minSurface,
            @Param("maxSurface") Double maxSurface,
            @Param("noTypeFilter") boolean noTypeFilter,
            @Param("wantAppartement") boolean wantAppartement,
            @Param("wantMaison") boolean wantMaison,
            Pageable pageable
    );
}
