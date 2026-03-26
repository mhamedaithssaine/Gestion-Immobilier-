package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProprietaireRepository extends JpaRepository<Proprietaire, UUID> {
    List<Proprietaire> findAllByOrderByCreatedAtDesc();
}
