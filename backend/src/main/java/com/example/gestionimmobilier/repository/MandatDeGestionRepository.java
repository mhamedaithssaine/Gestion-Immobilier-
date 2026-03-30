package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MandatDeGestionRepository extends JpaRepository<MandatDeGestion, UUID> {

    List<MandatDeGestion> findAllByOrderByDateDebutDesc();

    List<MandatDeGestion> findByStatutOrderByDateDebutDesc(StatutMandat statut);

    List<MandatDeGestion> findByProprietaire_IdOrderByDateDebutDesc(UUID proprietaireId);

    List<MandatDeGestion> findByProprietaire_KeycloakIdOrderByDateDebutDesc(String keycloakId);

    long countByProprietaire_KeycloakId(String keycloakId);

    long countByProprietaire_KeycloakIdAndStatut(String keycloakId, StatutMandat statut);

    List<MandatDeGestion> findByAgent_IdOrderByDateDebutDesc(UUID agentId);

    List<MandatDeGestion> findByBien_IdOrderByDateDebutDesc(UUID bienId);

    Optional<MandatDeGestion> findByBien_IdAndStatut(UUID bienId, StatutMandat statut);

    long countByAgent_Agence_Id(UUID agenceId);
    long countByAgent_Agence_IdAndStatut(UUID agenceId, StatutMandat statut);

    long countByAgent_Id(UUID agentId);
    long countByAgent_IdAndStatut(UUID agentId, StatutMandat statut);

    @Query("SELECT COUNT(m) FROM MandatDeGestion m WHERE m.agent.keycloakId = :keycloakId AND m.statut = :statut")
    long countByAgentKeycloakIdAndStatut(@Param("keycloakId") String keycloakId, @Param("statut") StatutMandat statut);

    @Query("SELECT COUNT(m) FROM MandatDeGestion m WHERE m.agent.keycloakId = :keycloakId")
    long countByAgentKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("SELECT COUNT(DISTINCT m.bien.id) FROM MandatDeGestion m WHERE m.agent.keycloakId = :keycloakId")
    long countDistinctBiensPourAgentKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("""
            SELECT COUNT(DISTINCT m.bien.id) FROM MandatDeGestion m
            WHERE m.agent.keycloakId = :keycloakId AND m.statut = :mandatStatut
            AND m.bien.statut = :statutBien""")
    long countDistinctBiensPourAgentMandatEtStatutBien(
            @Param("keycloakId") String keycloakId,
            @Param("mandatStatut") StatutMandat mandatStatut,
            @Param("statutBien") StatutBien statutBien);
}
