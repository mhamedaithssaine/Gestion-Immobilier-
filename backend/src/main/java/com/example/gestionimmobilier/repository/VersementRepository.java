package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.finance.Versement;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VersementRepository extends JpaRepository<Versement, UUID> {

    boolean existsByReferencePaiement(String referencePaiement);
    List<Versement> findByBailId(UUID bailId);
    List<Versement> findByBailIdAndDateVersementBetween(UUID bailId, LocalDateTime debut, LocalDateTime fin);
    List<Versement> findByProprietaire_IdOrderByDateVersementDesc(UUID proprietaireId);
    List<Versement> findByDateVersementBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("""
            SELECT COALESCE(SUM(v.montant), 0) FROM Versement v
            WHERE v.dateVersement >= :debut AND v.dateVersement <= :fin
            AND (
              (v.bail.agent IS NOT NULL AND v.bail.agent.keycloakId = :keycloakId)
              OR EXISTS (
                SELECT 1 FROM MandatDeGestion m
                WHERE m.bien.id = v.bail.bien.id AND m.statut = :mandatActif
                AND m.agent IS NOT NULL AND m.agent.keycloakId = :keycloakId
              )
            )""")
    BigDecimal sumMontantPourAgentEntre(
            @Param("keycloakId") String keycloakId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("mandatActif") StatutMandat mandatActif);

    @Query("""
            SELECT COALESCE(SUM(v.montant), 0) FROM Versement v
            WHERE v.proprietaire.keycloakId = :kc AND v.valide = true
            AND v.dateVersement >= :debut AND v.dateVersement <= :fin""")
    BigDecimal sumMontantPourProprietaireEntre(
            @Param("kc") String keycloakId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);
}
