package com.example.gestionimmobilier.service.agence;

import com.example.gestionimmobilier.dto.agence.AgenceDemandeEnAttenteResponse;
import com.example.gestionimmobilier.dto.agence.AgenceEspaceAgentResponse;
import com.example.gestionimmobilier.dto.agence.AgenceModificationDemandeAdminResponse;
import com.example.gestionimmobilier.dto.agence.AgenceResponse;
import com.example.gestionimmobilier.dto.agence.UpdateAgenceRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.agence.Agence;
import com.example.gestionimmobilier.models.entity.agence.AgenceModificationDemande;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.enums.StatutAgence;
import com.example.gestionimmobilier.models.enums.StatutDemandeModificationAgence;
import com.example.gestionimmobilier.models.enums.TypeDemandeModificationAgence;
import com.example.gestionimmobilier.repository.AgenceModificationDemandeRepository;
import com.example.gestionimmobilier.repository.AgenceRepository;
import com.example.gestionimmobilier.repository.AgentRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.user.KeycloakAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgenceModificationDemandeService {

    private final AgenceModificationDemandeRepository demandeRepository;
    private final AgenceRepository agenceRepository;
    private final AgenceService agenceService;
    private final AgentRepository agentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakAdminService keycloakAdminService;

    public AgenceModificationDemandeService(
            AgenceModificationDemandeRepository demandeRepository,
            AgenceRepository agenceRepository,
            AgenceService agenceService,
            AgentRepository agentRepository,
            UtilisateurRepository utilisateurRepository,
            KeycloakAdminService keycloakAdminService) {
        this.demandeRepository = demandeRepository;
        this.agenceRepository = agenceRepository;
        this.agenceService = agenceService;
        this.agentRepository = agentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Transactional(readOnly = true)
    public AgenceEspaceAgentResponse getEspaceAgent(String keycloakId) {
        Agence agence = agenceService.requireAgenceEntityPourAgent(keycloakId);
        AgenceResponse pub = agenceService.getById(agence.getId());
        return new AgenceEspaceAgentResponse(pub, mapPending(agence.getId()));
    }

    @Transactional
    public AgenceEspaceAgentResponse soumettreMiseAJour(String keycloakId, UpdateAgenceRequest request) {
        Agence agence = agenceService.requireAgenceEntityPourAgent(keycloakId);
        assertPeutDemander(agence);
        if (!aUneProposition(request)) {
            throw new ValidationException(ErrorMessages.AGENCE_DEMANDE_CHAMPS_VIDES);
        }
        if (request.email() != null && !request.email().isBlank()) {
            String em = request.email().trim();
            if (!em.equalsIgnoreCase(agence.getEmail()) && agenceRepository.existsByEmailAndIdNot(em, agence.getId())) {
                throw new ValidationException(ErrorMessages.AGENCE_EMAIL_DEJA_UTILISE);
            }
        }
        annulerDemandesEnAttente(agence.getId());

        AgenceModificationDemande d = AgenceModificationDemande.builder()
                .agence(agence)
                .type(TypeDemandeModificationAgence.MISE_AJOUR)
                .statut(StatutDemandeModificationAgence.EN_ATTENTE)
                .nomPropose(blankToNull(request.nom()))
                .emailPropose(blankToNull(request.email()))
                .telephonePropose(blankToNull(request.telephone()))
                .adressePropose(blankToNull(request.adresse()))
                .villePropose(blankToNull(request.ville()))
                .demandeurKeycloakId(keycloakId)
                .build();
        demandeRepository.save(d);

        AgenceResponse pub = agenceService.getById(agence.getId());
        return new AgenceEspaceAgentResponse(pub, toPendingResponse(d));
    }

    @Transactional
    public AgenceEspaceAgentResponse soumettreSuppression(String keycloakId) {
        Agence agence = agenceService.requireAgenceEntityPourAgent(keycloakId);
        assertPeutDemander(agence);
        if (agence.getStatut() == StatutAgence.SUSPENDED || agence.getStatut() == StatutAgence.REJECTED) {
            throw new ValidationException(ErrorMessages.AGENCE_MODIFICATION_NON_AUTORISEE);
        }
        annulerDemandesEnAttente(agence.getId());

        AgenceModificationDemande d = AgenceModificationDemande.builder()
                .agence(agence)
                .type(TypeDemandeModificationAgence.SUPPRESSION)
                .statut(StatutDemandeModificationAgence.EN_ATTENTE)
                .demandeurKeycloakId(keycloakId)
                .build();
        demandeRepository.save(d);

        AgenceResponse pub = agenceService.getById(agence.getId());
        return new AgenceEspaceAgentResponse(pub, toPendingResponse(d));
    }

    @Transactional(readOnly = true)
    public List<AgenceModificationDemandeAdminResponse> listerDemandesEnAttenteAdmin() {
        return demandeRepository.findByStatutOrderByCreatedAtAsc(StatutDemandeModificationAgence.EN_ATTENTE).stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public AgenceModificationDemandeAdminResponse approuverDemandeAdmin(UUID demandeId) {
        AgenceModificationDemande d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_DEMANDE_INTROUVABLE));
        if (d.getStatut() != StatutDemandeModificationAgence.EN_ATTENTE) {
            throw new ValidationException(ErrorMessages.AGENCE_DEMANDE_DEJA_TRAITEE);
        }
        Agence agence = d.getAgence();
        if (d.getType() == TypeDemandeModificationAgence.MISE_AJOUR) {
            UpdateAgenceRequest req = new UpdateAgenceRequest(
                    d.getNomPropose(),
                    d.getEmailPropose(),
                    d.getTelephonePropose(),
                    d.getAdressePropose(),
                    d.getVillePropose());
            agenceService.mettreAJourAgenceAdmin(agence.getId(), req);
        } else {
            suspendreAgenceEtDesactiverAgents(agence);
        }
        d.setStatut(StatutDemandeModificationAgence.APPROUVE);
        d.setResoluLe(LocalDateTime.now());
        demandeRepository.save(d);
        return toAdminResponse(d);
    }

    @Transactional
    public AgenceModificationDemandeAdminResponse rejeterDemandeAdmin(UUID demandeId, String commentaire) {
        AgenceModificationDemande d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_DEMANDE_INTROUVABLE));
        if (d.getStatut() != StatutDemandeModificationAgence.EN_ATTENTE) {
            throw new ValidationException(ErrorMessages.AGENCE_DEMANDE_DEJA_TRAITEE);
        }
        d.setStatut(StatutDemandeModificationAgence.REJETE);
        d.setResoluLe(LocalDateTime.now());
        d.setCommentaireAdmin(commentaire != null && !commentaire.isBlank() ? commentaire.trim() : null);
        demandeRepository.save(d);
        return toAdminResponse(d);
    }

    private void suspendreAgenceEtDesactiverAgents(Agence agence) {
        agence.setStatut(StatutAgence.SUSPENDED);
        agenceRepository.save(agence);
        for (Agent a : agentRepository.findByAgence_Id(agence.getId())) {
            keycloakAdminService.setUserEnabled(a.getKeycloakId(), false);
            a.setEnabled(false);
            utilisateurRepository.save(a);
        }
    }

    private static void assertPeutDemander(Agence agence) {
        StatutAgence st = agence.getStatut();
        if (st != StatutAgence.PENDING && st != StatutAgence.ACTIVE) {
            throw new ValidationException(ErrorMessages.AGENCE_MODIFICATION_NON_AUTORISEE);
        }
    }

    private void annulerDemandesEnAttente(UUID agenceId) {
        List<AgenceModificationDemande> pending =
                demandeRepository.findByAgence_IdAndStatut(agenceId, StatutDemandeModificationAgence.EN_ATTENTE);
        demandeRepository.deleteAll(pending);
    }

    private AgenceDemandeEnAttenteResponse mapPending(UUID agenceId) {
        List<AgenceModificationDemande> list =
                demandeRepository.findByAgence_IdAndStatut(agenceId, StatutDemandeModificationAgence.EN_ATTENTE);
        if (list.isEmpty()) {
            return null;
        }
        return toPendingResponse(list.get(list.size() - 1));
    }

    private static AgenceDemandeEnAttenteResponse toPendingResponse(AgenceModificationDemande d) {
        return new AgenceDemandeEnAttenteResponse(
                d.getId(),
                d.getType(),
                d.getNomPropose(),
                d.getEmailPropose(),
                d.getTelephonePropose(),
                d.getAdressePropose(),
                d.getVillePropose(),
                d.getCreatedAt());
    }

    private AgenceModificationDemandeAdminResponse toAdminResponse(AgenceModificationDemande d) {
        Agence a = d.getAgence();
        return new AgenceModificationDemandeAdminResponse(
                d.getId(),
                a.getId(),
                a.getNom(),
                d.getType(),
                d.getStatut(),
                d.getNomPropose(),
                d.getEmailPropose(),
                d.getTelephonePropose(),
                d.getAdressePropose(),
                d.getVillePropose(),
                d.getCreatedAt(),
                d.getDemandeurKeycloakId());
    }

    private static boolean aUneProposition(UpdateAgenceRequest r) {
        return (r.nom() != null && !r.nom().isBlank())
                || (r.email() != null && !r.email().isBlank())
                || (r.telephone() != null && !r.telephone().isBlank())
                || (r.adresse() != null && !r.adresse().isBlank())
                || (r.ville() != null && !r.ville().isBlank());
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
