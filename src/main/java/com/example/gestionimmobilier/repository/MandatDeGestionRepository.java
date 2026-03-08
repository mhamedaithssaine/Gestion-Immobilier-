package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MandatDeGestionRepository extends JpaRepository<MandatDeGestion, UUID> {

    List<MandatDeGestion> findAllByOrderByDateDebutDesc();

    List<MandatDeGestion> findByStatutOrderByDateDebutDesc(StatutMandat statut);

    List<MandatDeGestion> findByProprietaire_IdOrderByDateDebutDesc(UUID proprietaireId);

    List<MandatDeGestion> findByBien_IdOrderByDateDebutDesc(UUID bienId);

    Optional<MandatDeGestion> findByBien_IdAndStatut(UUID bienId, StatutMandat statut);
}
