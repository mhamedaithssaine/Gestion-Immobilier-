package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.finance.Versement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VersementRepository extends JpaRepository<Versement, UUID> {

    boolean existsByReferencePaiement(String referencePaiement);
    List<Versement> findByBailId(UUID bailId);
    List<Versement> findByBailIdAndDateVersementBetween(UUID bailId, LocalDateTime debut, LocalDateTime fin);
    List<Versement> findByProprietaire_IdOrderByDateVersementDesc(UUID proprietaireId);
    List<Versement> findByDateVersementBetween(LocalDateTime debut, LocalDateTime fin);
}
