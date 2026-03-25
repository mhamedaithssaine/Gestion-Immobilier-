package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.enums.StatutBien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BienImmobilierRepository extends JpaRepository<BienImmobilier, UUID> {

    boolean existsByReference(String reference);

    List<BienImmobilier> findByProprietaireOrderByCreatedAtDesc(Proprietaire proprietaire);

    Optional<BienImmobilier> findByIdAndProprietaire(UUID id, Proprietaire proprietaire);

    long countByStatut(StatutBien statut);
}
