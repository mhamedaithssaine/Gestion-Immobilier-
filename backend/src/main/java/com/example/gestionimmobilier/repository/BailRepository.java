package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.enums.StatutBail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BailRepository extends JpaRepository<Bail, UUID> {

    List<Bail> findAllByOrderByDateDebutDesc();

    List<Bail> findByStatutOrderByDateDebutDesc(StatutBail statut);

    List<Bail> findByClient_IdOrderByDateDebutDesc(UUID clientId);

    boolean existsByBien_Id(UUID bienId);

   
    @Query("SELECT COUNT(b) > 0 FROM Bail b WHERE b.bien.id = :bienId AND b.statut IN :statuts")
    boolean existsByBien_IdAndStatutIn(@Param("bienId") UUID bienId, @Param("statuts") List<StatutBail> statuts);
}

