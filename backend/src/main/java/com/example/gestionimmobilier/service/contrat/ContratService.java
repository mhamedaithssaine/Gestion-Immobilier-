package com.example.gestionimmobilier.service.contrat;

import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.CreateContratRequest;
import com.example.gestionimmobilier.dto.contrat.UpdateContratRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ForbiddenException;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.BienMapper;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ContratService {

    private static final Logger log = LoggerFactory.getLogger(ContratService.class);

    /** Empêche une nouvelle demande / contrat tant qu’une demande ou un bail actif existe sur le bien. */
    private static final List<StatutBail> STATUTS_BLOQUANTS_NOUVEAU_BAIL = List.of(
            StatutBail.ACTIF,
            StatutBail.EN_ATTENTE,
            StatutBail.EN_ATTENTE_VALIDATION_AGENT
    );

    private final BailRepository bailRepository;
    private final BienImmobilierRepository bienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MandatDeGestionRepository mandatRepository;
    private final BienMapper bienMapper;
    private final UserMapper userMapper;

    public ContratService(BailRepository bailRepository,
                          BienImmobilierRepository bienRepository,
                          UtilisateurRepository utilisateurRepository,
                          MandatDeGestionRepository mandatRepository,
                          BienMapper bienMapper,
                          UserMapper userMapper) {
        this.bailRepository = bailRepository;
        this.bienRepository = bienRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.mandatRepository = mandatRepository;
        this.bienMapper = bienMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public ContratResponse creerContrat(CreateContratRequest request) {
        log.info("Création contrat bienId={} proprietaireId={} locataireId={} agentId={}",
                request.bienId(), request.proprietaireId(), request.locataireId(), request.agentId());

        BienImmobilier bien = bienRepository.findById(request.bienId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        Utilisateur uProprio = utilisateurRepository.findById(request.proprietaireId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE));
        if (!(uProprio instanceof Proprietaire proprietaire)) {
            throw new ValidationException(ErrorMessages.PROPRIETAIRE_INTROUVABLE);
        }

        Utilisateur uClient = utilisateurRepository.findById(request.locataireId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE));
        if (!(uClient instanceof Client locataire)) {
            throw new ValidationException(ErrorMessages.CLIENT_INTROUVABLE);
        }

        Agent agent = null;
        if (request.agentId() != null) {
            Utilisateur uAgent = utilisateurRepository.findById(request.agentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
            if (uAgent instanceof Agent a) {
                agent = a;
            } else {
                throw new ValidationException(ErrorMessages.UTILISATEUR_N_EST_PAS_AGENT);
            }
        } else {
            agent = mandatRepository.findByBien_IdAndStatut(bien.getId(), StatutMandat.ACTIF)
                    .map(MandatDeGestion::getAgent)
                    .orElse(null);
        }

        if (!bien.getProprietaire().getId().equals(proprietaire.getId())) {
            throw new ValidationException(ErrorMessages.BIEN_N_APPARTIENT_PAS_PROPRIETAIRE);
        }

        if (bailRepository.existsByBien_IdAndStatutIn(bien.getId(), STATUTS_BLOQUANTS_NOUVEAU_BAIL)) {
            throw new ValidationException(ErrorMessages.BIEN_DEJA_LIE_CONTRAT);
        }
        if (bien.getStatut() != StatutBien.DISPONIBLE) {
            throw new ValidationException(ErrorMessages.BIEN_NON_DISPONIBLE_POUR_LOCATION);
        }

        String numContrat = "CONTRAT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Bail bail = Bail.builder()
                .numContrat(numContrat)
                .dateSignature(request.dateSignature() != null ? request.dateSignature() : LocalDate.now())
                .documentUrl(request.documentUrl())
                .client(locataire)
                .proprietaire(proprietaire)
                .agent(agent)
                .bien(bien)
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .loyerHC(request.loyerHC())
                .charges(request.charges())
                .statut(StatutBail.ACTIF)
                .build();

        bail = bailRepository.save(bail);
        bien.setStatut(StatutBien.LOUE);
        bienRepository.save(bien);
        log.info("Contrat créé id={} numContrat={} statut={}", bail.getId(), bail.getNumContrat(), bail.getStatut());
        return toContratResponse(bail);
    }

    public List<ContratResponse> listerContrats(StatutBail statut) {
        List<Bail> baux = statut == null
                ? bailRepository.findAllByOrderByDateDebutDesc()
                : bailRepository.findByStatutOrderByDateDebutDesc(statut);
        return baux.stream().map(this::toContratResponse).toList();
    }

    public ContratResponse getContratById(UUID id) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        return toContratResponse(bail);
    }

    @Transactional
    public ContratResponse modifierContrat(UUID id, UpdateContratRequest request) {
        log.info("Modification contrat {} (dateDebut={}, dateFin={}, loyerHC={}, charges={})",
                id, request.dateDebut(), request.dateFin(), request.loyerHC(), request.charges());
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (bail.getStatut() == StatutBail.RESILIE || bail.getStatut() == StatutBail.TERMINE
                || bail.getStatut() == StatutBail.REFUSE) {
            throw new ValidationException(ErrorMessages.CONTRAT_NON_MODIFIABLE);
        }
        if (request.dateDebut() != null) bail.setDateDebut(request.dateDebut());
        if (request.dateFin() != null) bail.setDateFin(request.dateFin());
        if (request.loyerHC() != null) bail.setLoyerHC(request.loyerHC());
        if (request.charges() != null) bail.setCharges(request.charges());
        if (request.dateSignature() != null) bail.setDateSignature(request.dateSignature());
        if (request.documentUrl() != null) bail.setDocumentUrl(request.documentUrl());
        bail = bailRepository.save(bail);
        log.info("Contrat modifié id={} statut={}", bail.getId(), bail.getStatut());
        return toContratResponse(bail);
    }

    @Transactional
    public ContratResponse resilierContrat(UUID id) {
        log.info("Résiliation contrat {}", id);
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (bail.getStatut() == StatutBail.REFUSE) {
            throw new ValidationException(ErrorMessages.CONTRAT_DEMANDE_REFUSEE_NON_RESILIABLE);
        }
        if (bail.getStatut() == StatutBail.RESILIE || bail.getStatut() == StatutBail.TERMINE) {
            throw new ValidationException(ErrorMessages.CONTRAT_DEJA_RESILIE);
        }
        bail.setStatut(StatutBail.RESILIE);
        bail = bailRepository.save(bail);
        BienImmobilier bien = bail.getBien();
        bien.setStatut(StatutBien.DISPONIBLE);
        bienRepository.save(bien);
        log.info("Contrat résilié id={} bienId={} nouveauStatutBien={}", bail.getId(), bien.getId(), bien.getStatut());
        return toContratResponse(bail);
    }

    public List<ContratResponse> listerContratsParLocataire(UUID locataireId) {
        List<Bail> baux = bailRepository.findByClient_IdOrderByDateDebutDesc(locataireId);
        return baux.stream().map(this::toContratResponse).toList();
    }

    public List<ContratResponse> listerContratsParLocataireKeycloak(String keycloakId) {
        List<Bail> baux = bailRepository.findByClient_KeycloakIdOrderByDateDebutDesc(keycloakId);
        return baux.stream().map(this::toContratResponse).toList();
    }

    public List<ContratResponse> listerContratsParProprietaireKeycloak(String keycloakId) {
        List<Bail> baux = bailRepository.findByProprietaire_KeycloakIdOrderByDateDebutDesc(keycloakId);
        return baux.stream().map(this::toContratResponse).toList();
    }

    @Transactional
    public ContratResponse resilierContratProprietaire(UUID id, String keycloakId) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (!bail.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return resilierContrat(id);
    }

    @Transactional
    public ContratResponse modifierContratProprietaire(UUID id, String keycloakId, UpdateContratRequest request) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (!bail.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        refuserSiBailSousMandatActif(bail);
        return modifierContrat(id, request);
    }

    @Transactional
    public ContratResponse changerStatutContratProprietaire(UUID id, String keycloakId, StatutBail statut) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (!bail.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        refuserSiDemandeReserveeAgent(bail);
        return changerStatutContrat(id, statut);
    }

    /**
     * Mandat actif : le propriétaire ne modifie pas les termes du bail (pilotage agent).
     */
    private void refuserSiBailSousMandatActif(Bail bail) {
        if (mandatRepository.findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF).isPresent()) {
            throw new ForbiddenException(ErrorMessages.CONTRAT_GERE_PAR_MANDATAIRE);
        }
    }

    /**
     * File d’attente agent ou ancienne demande EN_ATTENTE alors qu’un mandat existe : pas le propriétaire.
     */
    private void refuserSiDemandeReserveeAgent(Bail bail) {
        if (bail.getStatut() == StatutBail.EN_ATTENTE_VALIDATION_AGENT) {
            throw new ForbiddenException(ErrorMessages.CONTRAT_VALIDATION_AGENT_REQUISE);
        }
        if (bail.getStatut() == StatutBail.EN_ATTENTE
                && mandatRepository.findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF).isPresent()) {
            throw new ForbiddenException(ErrorMessages.CONTRAT_VALIDATION_AGENT_REQUISE);
        }
    }

    public List<ContratResponse> listerContratsParAgentKeycloak(String keycloakId, StatutBail statut) {
        List<Bail> baux = statut == null
                ? bailRepository.findByAgent_KeycloakIdOrderByDateDebutDesc(keycloakId, StatutMandat.ACTIF)
                : bailRepository.findByAgent_KeycloakIdAndStatutOrderByDateDebutDesc(
                        keycloakId, statut, StatutMandat.ACTIF);
        return baux.stream().map(this::toContratResponse).toList();
    }

    public ContratResponse getContratByIdPourAgent(UUID id, String keycloakId) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        assertAgentPeutVoirBail(bail, keycloakId);
        return toContratResponse(bail);
    }

    @Transactional
    public ContratResponse modifierContratAgent(UUID id, String keycloakId, UpdateContratRequest request) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        assertAgentMandataireDuBien(bail, keycloakId);
        return modifierContrat(id, request);
    }

   
    @Transactional
    public ContratResponse changerStatutContratAgent(UUID id, String keycloakId, StatutBail nouveauStatut) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        assertAgentMandataireDuBien(bail, keycloakId);
        if (bail.getAgent() == null) {
            MandatDeGestion m = mandatRepository
                    .findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF)
                    .orElseThrow(() -> new ForbiddenException(ErrorMessages.CONTRAT_AGENT_HORS_MANDAT));
            bail.setAgent(m.getAgent());
            bailRepository.save(bail);
        }

        StatutBail current = bail.getStatut();
        boolean fileValidationAgent = current == StatutBail.EN_ATTENTE_VALIDATION_AGENT
                || (current == StatutBail.EN_ATTENTE
                && mandatRepository.findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF).isPresent());

        if (fileValidationAgent) {
            if (nouveauStatut != StatutBail.ACTIF && nouveauStatut != StatutBail.REFUSE) {
                throw new ValidationException(ErrorMessages.CONTRAT_TRANSITION_STATUT_INTERDITE);
            }
        } else {
            if (current == StatutBail.REFUSE || current == StatutBail.RESILIE || current == StatutBail.TERMINE) {
                throw new ValidationException(ErrorMessages.CONTRAT_NON_MODIFIABLE);
            }
        }

        return changerStatutContrat(id, nouveauStatut);
    }

    /** Lecture d’un bail : agent désigné sur le bail ou titulaire du mandat actif sur le bien. */
    private void assertAgentPeutVoirBail(Bail bail, String keycloakId) {
        if (bail.getAgent() != null
                && bail.getAgent().getKeycloakId() != null
                && bail.getAgent().getKeycloakId().equals(keycloakId)) {
            return;
        }
        boolean viaMandat = mandatRepository
                .findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF)
                .map(m -> m.getAgent() != null && keycloakId.equals(m.getAgent().getKeycloakId()))
                .orElse(false);
        if (!viaMandat) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
    }

    private void assertAgentMandataireDuBien(Bail bail, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findByBien_IdAndStatut(bail.getBien().getId(), StatutMandat.ACTIF)
                .orElseThrow(() -> new ForbiddenException(ErrorMessages.CONTRAT_AGENT_HORS_MANDAT));
        if (mandat.getAgent() == null || !mandat.getAgent().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.CONTRAT_AGENT_HORS_MANDAT);
        }
        if (bail.getAgent() != null && !bail.getAgent().getId().equals(mandat.getAgent().getId())) {
            throw new ValidationException(ErrorMessages.CONTRAT_AGENT_HORS_MANDAT);
        }
    }

    @Transactional
    public ContratResponse changerStatutContrat(UUID id, StatutBail statut) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (statut == null) {
            throw new ValidationException("Le statut du contrat est requis.");
        }

        if (statut == StatutBail.ACTIF) {
            boolean bienOccupeParAutre = bailRepository.existsByBien_IdAndStatutInAndIdNot(
                    bail.getBien().getId(),
                    List.of(StatutBail.ACTIF),
                    bail.getId()
            );
            if (bienOccupeParAutre) {
                throw new ValidationException(ErrorMessages.BIEN_DEJA_LIE_CONTRAT);
            }

            boolean locataireAvecAutreContratActif = bailRepository.existsByClient_IdAndStatutInAndIdNot(
                    bail.getClient().getId(),
                    List.of(StatutBail.ACTIF),
                    bail.getId()
            );
            if (locataireAvecAutreContratActif) {
                throw new ValidationException(ErrorMessages.LOCATAIRE_DEJA_LIE_CONTRAT_ACTIF);
            }
        }

        bail.setStatut(statut);
        bail = bailRepository.save(bail);

        BienImmobilier bien = bail.getBien();
        if (statut == StatutBail.ACTIF) {
            bien.setStatut(StatutBien.LOUE);
        } else if (statut == StatutBail.RESILIE || statut == StatutBail.TERMINE || statut == StatutBail.REFUSE) {
            bien.setStatut(StatutBien.DISPONIBLE);
        }
        bienRepository.save(bien);
        return toContratResponse(bail);
    }

    private ContratResponse toContratResponse(Bail bail) {
        BienResponse bienRes = bienMapper.toBienResponse(bail.getBien());
        ProprietaireResponse proprioRes = userMapper.toProprietaireResponse(bail.getProprietaire());
        LocataireResponse locataireRes = userMapper.toLocataireResponse(bail.getClient());
        Agent agent = bail.getAgent();
        String agentNomComplet = agent != null ? (agent.getFirstName() + " " + agent.getLastName()).trim() : null;
        return new ContratResponse(
                bail.getId(),
                bail.getNumContrat(),
                bail.getDateSignature(),
                bail.getDateDebut(),
                bail.getDateFin(),
                bail.getLoyerHC(),
                bail.getCharges(),
                bail.getDocumentUrl(),
                bail.getStatut(),
                bienRes,
                proprioRes,
                locataireRes,
                agent != null ? agent.getId() : null,
                agentNomComplet
        );
    }
}

