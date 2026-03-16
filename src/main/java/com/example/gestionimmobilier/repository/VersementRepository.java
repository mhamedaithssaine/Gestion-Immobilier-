package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.finance.Versement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VersementRepository extends JpaRepository<Versement, UUID> {

    boolean existsByReferencePaiement(String referencePaiement);
    List<Versement> findByBailId(UUID bailId);
}
