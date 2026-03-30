package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.agence.AgenceModificationDemande;
import com.example.gestionimmobilier.models.enums.StatutDemandeModificationAgence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AgenceModificationDemandeRepository extends JpaRepository<AgenceModificationDemande, UUID> {

    List<AgenceModificationDemande> findByAgence_IdAndStatut(UUID agenceId, StatutDemandeModificationAgence statut);

    List<AgenceModificationDemande> findByStatutOrderByCreatedAtAsc(StatutDemandeModificationAgence statut);

    @Query("""
            SELECT d FROM AgenceModificationDemande d
            WHERE d.agence.id = :agenceId AND d.statut = :statut
            AND d.commentaireAdmin IS NOT NULL
            ORDER BY d.resoluLe DESC NULLS LAST, d.createdAt DESC""")
    List<AgenceModificationDemande> findDerniersRejetsAvecNote(
            @Param("agenceId") UUID agenceId,
            @Param("statut") StatutDemandeModificationAgence statut);
}
