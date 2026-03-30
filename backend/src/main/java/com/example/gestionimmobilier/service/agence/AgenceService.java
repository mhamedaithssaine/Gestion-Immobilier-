package com.example.gestionimmobilier.service.agence;

import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.dto.agence.CreateAgenceRequest;
import com.example.gestionimmobilier.dto.agence.UpdateAgenceRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.agence.Agence;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
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

    @Transactional
    public AgenceResponse mettreAJourAgenceAdmin(UUID id, UpdateAgenceRequest request) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));

        if (request.nom() != null && !request.nom().isBlank()) {
            agence.setNom(request.nom().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            if (!newEmail.equalsIgnoreCase(agence.getEmail())
                    && agenceRepository.existsByEmailAndIdNot(newEmail, agence.getId())) {
                throw new ValidationException(ErrorMessages.AGENCE_EMAIL_DEJA_UTILISE);
            }
            agence.setEmail(newEmail);
        }
        if (request.telephone() != null) {
            agence.setTelephone(request.telephone().isBlank() ? null : request.telephone().trim());
        }
        if (request.adresse() != null) {
            agence.setAdresse(request.adresse().isBlank() ? null : request.adresse().trim());
        }
        if (request.ville() != null) {
            agence.setVille(request.ville().isBlank() ? resolveVille(null, agence.getAdresse()) : request.ville().trim());
        }

        agence = agenceRepository.save(agence);
        for (Agent a : agentRepository.findByAgence_Id(agence.getId())) {
            a.setAgenceNom(agence.getNom());
            utilisateurRepository.save(a);
        }
        return toResponse(agence);
    }

    @Transactional
    public void supprimerAgenceAdmin(UUID id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        if (agentRepository.existsByAgence_Id(id)) {
            throw new ValidationException(ErrorMessages.AGENCE_SUPPRESSION_IMPOSSIBLE);
        }
        agenceRepository.delete(agence);
    }

    @Transactional
    public AgenceResponse getAgencePourAgentConnecte(String keycloakId) {
        return toResponse(requireAgenceEntityPourAgent(keycloakId));
    }

    /**
     * Agence résolue pour un agent (FK ou rétrocompatibilité {@code agenceNom}).
     */
    @Transactional
    public Agence requireAgenceEntityPourAgent(String keycloakId) {
        Agent agent = getAgentOuErreur(keycloakId);
        Agence agence = resolveAgencePourAgent(agent);
        if (agence == null) {
            throw new ResourceNotFoundException(ErrorMessages.AGENT_SANS_AGENCE);
        }
        return agence;
    }

    /**
     * Backward-compatibilité:
     * certains agents historiques n'ont pas la FK `agence` renseignée mais ont `agenceNom`.
     * On tente de relier automatiquement par nom d'agence.
     */
    private Agence resolveAgencePourAgent(Agent agent) {
        if (agent.getAgence() != null) {
            return agent.getAgence();
        }
        String agenceNom = agent.getAgenceNom();
        if (agenceNom == null || agenceNom.isBlank()) {
            return null;
        }
        return agenceRepository.findTopByNomIgnoreCaseOrderByCreatedAtDesc(agenceNom.trim())
                .map(found -> {
                    agent.setAgence(found);
                    if (agent.getAgenceNom() == null || agent.getAgenceNom().isBlank()) {
                        agent.setAgenceNom(found.getNom());
                    }
                    utilisateurRepository.save(agent);
                    return found;
                })
                .orElse(null);
    }

    private Agent getAgentOuErreur(String keycloakId) {
        Utilisateur u = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.COMPTE_NON_AGENT));
        if (!(u instanceof Agent found)) {
            throw new ResourceNotFoundException(ErrorMessages.COMPTE_NON_AGENT);
        }
        return found;
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
