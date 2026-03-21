package com.example.gestionimmobilier.service.agence;

import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.dto.agence.CreateAgenceRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.agence.Agence;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutAgence;
import com.example.gestionimmobilier.repository.AgenceRepository;
import com.example.gestionimmobilier.repository.AgentRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.user.KeycloakAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AgenceService {

    private final AgenceRepository agenceRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;

    public AgenceService(AgenceRepository agenceRepository,
                         KeycloakAdminService keycloakAdminService,
                         UtilisateurRepository utilisateurRepository,
                         AgentRepository agentRepository) {
        this.agenceRepository = agenceRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.utilisateurRepository = utilisateurRepository;
        this.agentRepository = agentRepository;
    }

    @Transactional
    public AgenceResponse inscrire(CreateAgenceRequest request) {
        if (agenceRepository.existsByEmail(request.email())) {
            throw new ValidationException(ErrorMessages.AGENCE_EMAIL_DEJA_UTILISE);
        }
        if (utilisateurRepository.existsByEmail(request.agentEmail())) {
            throw new ValidationException(ErrorMessages.AGENCE_CONTACT_EMAIL_DEJA_UTILISE);
        }
        if (utilisateurRepository.existsByUsername(request.agentUsername())) {
            throw new ValidationException(ErrorMessages.AGENCE_CONTACT_USERNAME_DEJA_UTILISE);
        }

        String ville = resolveVille(request.ville(), request.adresse());
        Agence agence = Agence.builder()
                .nom(request.nom())
                .email(request.email())
                .telephone(request.telephone())
                .adresse(request.adresse())
                .ville(ville)
                .statut(StatutAgence.PENDING)
                .build();
        agence = agenceRepository.save(agence);

        String keycloakUserId = keycloakAdminService.createUserInKeycloak(
                request.agentUsername(),
                request.agentEmail(),
                request.agentFirstName(),
                request.agentLastName(),
                request.agentPassword(),
                List.of(Role.ROLE_AGENT),
                false
        );

        String matricule = generateAgentMatricule(agence);
        Agent agent = Agent.builder()
                .keycloakId(keycloakUserId)
                .username(request.agentUsername())
                .email(request.agentEmail())
                .firstName(request.agentFirstName())
                .lastName(request.agentLastName())
                .roles(List.of(Role.ROLE_AGENT))
                .emailVerified(true)
                .enabled(false)
                .agence(agence)
                .agenceNom(agence.getNom())
                .matricule(matricule)
                .build();
        utilisateurRepository.save(agent);

        return toResponse(agence);
    }

    public List<AgenceResponse> listerToutes() {
        return agenceRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AgenceResponse> listerEnAttente() {
        return agenceRepository.findByStatutOrderByCreatedAtDesc(StatutAgence.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    public AgenceResponse getById(UUID id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        return toResponse(agence);
    }

    @Transactional
    public AgenceResponse approuver(UUID id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        if (agence.getStatut() == StatutAgence.ACTIVE) {
            throw new ValidationException(ErrorMessages.AGENCE_DEJA_ACTIVE);
        }
        agence.setStatut(StatutAgence.ACTIVE);
        agence = agenceRepository.save(agence);

        List<Agent> agents = agentRepository.findByAgence_Id(id);
        for (Agent agent : agents) {
            keycloakAdminService.setUserEnabled(agent.getKeycloakId(), true);
            agent.setEnabled(true);
            utilisateurRepository.save(agent);
        }

        return toResponse(agence);
    }

    @Transactional
    public AgenceResponse rejeter(UUID id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        agence.setStatut(StatutAgence.REJECTED);
        agence = agenceRepository.save(agence);
        return toResponse(agence);
    }

    @Transactional
    public AgenceResponse suspendre(UUID id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        agence.setStatut(StatutAgence.SUSPENDED);
        agence = agenceRepository.save(agence);
        return toResponse(agence);
    }

    private AgenceResponse toResponse(Agence agence) {
        return new AgenceResponse(
                agence.getId(),
                agence.getNom(),
                agence.getEmail(),
                agence.getTelephone(),
                agence.getAdresse(),
                agence.getVille(),
                agence.getStatut(),
                agence.getCreatedAt()
        );
    }

    private static String resolveVille(String villeFromRequest, String adresse) {
        if (villeFromRequest != null && !villeFromRequest.isBlank()) {
            return villeFromRequest.trim();
        }
        if (adresse == null || adresse.isBlank()) {
            return "SIEGE";
        }
        int lastComma = adresse.lastIndexOf(',');
        if (lastComma >= 0 && lastComma < adresse.length() - 1) {
            return adresse.substring(lastComma + 1).trim();
        }
        return "SIEGE";
    }

    private static String toVilleCodeForMatricule(String ville) {
        if (ville == null || ville.isBlank()) {
            return "SIEGE";
        }
        return ville.trim()
                .toUpperCase()
                .replaceAll("\\s+", "-");
    }

    private String generateAgentMatricule(Agence agence) {
        String villeCode = toVilleCodeForMatricule(agence.getVille());
        String prefix = "AGENCE-" + villeCode + "-";
        int nextNum = 1;
        java.util.Optional<Agent> last = agentRepository.findTopByMatriculeStartingWithOrderByMatriculeDesc(prefix);
        if (last.isPresent() && last.get().getMatricule() != null && last.get().getMatricule().length() >= prefix.length() + 4) {
            try {
                String numPart = last.get().getMatricule().substring(prefix.length());
                nextNum = Integer.parseInt(numPart, 10) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", nextNum);
    }
}
