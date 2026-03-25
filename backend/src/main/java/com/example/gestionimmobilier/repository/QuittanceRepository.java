package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.finance.Quittance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuittanceRepository extends JpaRepository<Quittance, UUID> {
    Optional<Quittance> findByVersement_Id(UUID versementId);
}
