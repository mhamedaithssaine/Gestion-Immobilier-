package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BailRepository extends JpaRepository<Bail, UUID> {

    List<Bail> findAllByOrderByDateDebutDesc();

    List<Bail> findByStatutOrderByDateDebutDesc(StatutBail statut);

    List<Bail> findByClient_IdOrderByDateDebutDesc(UUID clientId);

    List<Bail> findByClient_KeycloakIdOrderByDateDebutDesc(String keycloakId);

    List<Bail> findByProprietaire_KeycloakIdOrderByDateDebutDesc(String keycloakId);

    long countByProprietaire_KeycloakId(String keycloakId);

    long countByProprietaire_KeycloakIdAndStatut(String keycloakId, StatutBail statut);

    @Query("""
            SELECT COALESCE(SUM(b.loyerHC + COALESCE(b.charges, 0)), 0) FROM Bail b
            WHERE b.proprietaire.keycloakId = :kc AND b.statut = :statut""")
    java.math.BigDecimal sumLoyerEtChargesPourProprietaireStatut(
            @Param("kc") String keycloakId,
            @Param("statut") StatutBail statut);

    /**
     * Baux visibles par l’agent : soit {@code bail.agent} renseigné, soit mandat actif sur le bien avec le même agent.
     * Couvre les données historiques sans {@code agent_id} sur le bail.
     */
    @Query("""
            SELECT b FROM Bail b WHERE
              (b.agent IS NOT NULL AND b.agent.keycloakId = :keycloakId)
              OR EXISTS (
                SELECT 1 FROM MandatDeGestion m WHERE m.bien.id = b.bien.id AND m.statut = :mandatActif
                AND m.agent IS NOT NULL AND m.agent.keycloakId = :keycloakId
              )
            ORDER BY b.dateDebut DESC""")
    List<Bail> findByAgent_KeycloakIdOrderByDateDebutDesc(
            @Param("keycloakId") String keycloakId,
            @Param("mandatActif") StatutMandat mandatActif);

    @Query("""
            SELECT b FROM Bail b WHERE b.statut = :statut AND (
              (b.agent IS NOT NULL AND b.agent.keycloakId = :keycloakId)
              OR EXISTS (
                SELECT 1 FROM MandatDeGestion m WHERE m.bien.id = b.bien.id AND m.statut = :mandatActif
                AND m.agent IS NOT NULL AND m.agent.keycloakId = :keycloakId
              )
            ) ORDER BY b.dateDebut DESC""")
    List<Bail> findByAgent_KeycloakIdAndStatutOrderByDateDebutDesc(
            @Param("keycloakId") String keycloakId,
            @Param("statut") StatutBail statut,
            @Param("mandatActif") StatutMandat mandatActif);

    @Query("""
            SELECT COUNT(b) FROM Bail b WHERE b.statut = :statut AND (
              (b.agent IS NOT NULL AND b.agent.keycloakId = :keycloakId)
              OR EXISTS (
                SELECT 1 FROM MandatDeGestion m WHERE m.bien.id = b.bien.id AND m.statut = :mandatActif
                AND m.agent IS NOT NULL AND m.agent.keycloakId = :keycloakId
              )
            )""")
    long countByAgentKeycloakIdAndStatut(
            @Param("keycloakId") String keycloakId,
            @Param("statut") StatutBail statut,
            @Param("mandatActif") StatutMandat mandatActif);

    @Query("""
            SELECT COUNT(DISTINCT b.client.id) FROM Bail b WHERE b.statut = :statut AND (
              (b.agent IS NOT NULL AND b.agent.keycloakId = :keycloakId)
              OR EXISTS (
                SELECT 1 FROM MandatDeGestion m WHERE m.bien.id = b.bien.id AND m.statut = :mandatActif
                AND m.agent IS NOT NULL AND m.agent.keycloakId = :keycloakId
              )
            )""")
    long countDistinctClientsPourAgentEtStatut(
            @Param("keycloakId") String keycloakId,
            @Param("statut") StatutBail statut,
            @Param("mandatActif") StatutMandat mandatActif);

    boolean existsByBien_Id(UUID bienId);

   
    @Query("SELECT COUNT(b) > 0 FROM Bail b WHERE b.bien.id = :bienId AND b.statut IN :statuts")
    boolean existsByBien_IdAndStatutIn(@Param("bienId") UUID bienId, @Param("statuts") List<StatutBail> statuts);

    @Query("SELECT COUNT(b) > 0 FROM Bail b WHERE b.bien.id = :bienId AND b.statut IN :statuts AND b.id <> :excludeId")
    boolean existsByBien_IdAndStatutInAndIdNot(
            @Param("bienId") UUID bienId,
            @Param("statuts") List<StatutBail> statuts,
            @Param("excludeId") UUID excludeId
    );

    @Query("SELECT COUNT(b) > 0 FROM Bail b WHERE b.client.id = :clientId AND b.statut IN :statuts AND b.id <> :excludeId")
    boolean existsByClient_IdAndStatutInAndIdNot(
            @Param("clientId") UUID clientId,
            @Param("statuts") List<StatutBail> statuts,
            @Param("excludeId") UUID excludeId
    );
}

