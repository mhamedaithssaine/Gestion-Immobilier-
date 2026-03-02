package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdresseRepository extends JpaRepository<Adresse, UUID> {
}
