package com.example.gestionimmobilier.repository;

import com.example.gestionimmobilier.models.entity.user.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {

    List<Agent> findByAgence_Id(UUID agenceId);

    Optional<Agent> findTopByMatriculeStartingWithOrderByMatriculeDesc(String prefix);
}
