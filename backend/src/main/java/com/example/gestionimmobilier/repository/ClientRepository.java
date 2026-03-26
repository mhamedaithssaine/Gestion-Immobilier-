package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.user.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findAllByOrderByCreatedAtDesc();
}
