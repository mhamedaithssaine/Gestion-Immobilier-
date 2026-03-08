package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.enums.StatutBail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BailRepository extends JpaRepository<Bail, UUID> {

    List<Bail> findAllByOrderByDateDebutDesc();

    List<Bail> findByStatutOrderByDateDebutDesc(StatutBail statut);

    List<Bail> findByClient_IdOrderByDateDebutDesc(UUID clientId);

    boolean existsByBien_Id(UUID bienId);
}

