package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.agence.Agence;
import com.example.gestionimmobilier.models.enums.StatutAgence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgenceRepository extends JpaRepository<Agence, UUID> {

    List<Agence> findByStatutOrderByCreatedAtDesc(StatutAgence statut);

    List<Agence> findAllByOrderByCreatedAtDesc();

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<Agence> findTopByNomIgnoreCaseOrderByCreatedAtDesc(String nom);
}
