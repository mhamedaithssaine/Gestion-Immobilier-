package com.example.gestionimmobilier.service.locataire;

import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.CreateDemandeLocationRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ForbiddenException;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.BienMapper;
import com.example.gestionimmobilier.mapper.UserMapper;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.contrat.ContratService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocataireEspaceService {

    private static final List<StatutBail> STATUTS_BLOQUANTS_NOUVELLE_DEMANDE = List.of(
            StatutBail.ACTIF,
            StatutBail.EN_ATTENTE,
            StatutBail.EN_ATTENTE_VALIDATION_AGENT
    );

    private final BienImmobilierRepository bienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BailRepository bailRepository;
    private final MandatDeGestionRepository mandatRepository;
    private final BienMapper bienMapper;
    private final UserMapper userMapper;
    private final ContratService contratService;

    public LocataireEspaceService(BienImmobilierRepository bienRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  BailRepository bailRepository,
                                  MandatDeGestionRepository mandatRepository,
                                  BienMapper bienMapper,
                                  UserMapper userMapper,
                                  ContratService contratService) {
        this.bienRepository = bienRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.bailRepository = bailRepository;
        this.mandatRepository = mandatRepository;
        this.bienMapper = bienMapper;
        this.userMapper = userMapper;
        this.contratService = contratService;
    }

    @Transactional(readOnly = true)
    public Page<BienResponse> searchBiensDisponibles(String query,
                                                     String ville,
                                                     BigDecimal minPrix,
                                                     BigDecimal maxPrix,
                                                     Double minSurface,
                                                     Double maxSurface,
                                                     List<String> typesBien,
                                                     Pageable pageable) {
        boolean wantAppartement = false;
        boolean wantMaison = false;
        if (typesBien != null) {
            for (String raw : typesBien) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                String t = raw.trim().toUpperCase(Locale.ROOT);
                if ("APPARTEMENT".equals(t)) {
                    wantAppartement = true;
                } else if ("MAISON".equals(t)) {
                    wantMaison = true;
                }
            }
        }
        boolean noTypeFilter = !wantAppartement && !wantMaison;
        return bienRepository
                .searchDisponibles(
                        query, ville, minPrix, maxPrix, minSurface, maxSurface,
                        noTypeFilter, wantAppartement, wantMaison, pageable)
                .map(bienMapper::toBienResponse);
    }

    @Transactional(readOnly = true)
    public BienResponse getBienDisponibleById(UUID bienId) {
        BienImmobilier bien = bienRepository.findById(bienId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
        if (bien.getStatut() != com.example.gestionimmobilier.models.enums.StatutBien.DISPONIBLE) {
            throw new ValidationException(ErrorMessages.BIEN_NON_DISPONIBLE_POUR_LOCATION);
        }
        return bienMapper.toBienResponse(bien);
    }

    @Transactional
    public ContratResponse creerDemandeLocation(UUID bienId, String keycloakId, CreateDemandeLocationRequest req) {
        Client client = getClientByKeycloakId(keycloakId);
        BienImmobilier bien = bienRepository.findById(bienId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        if (bien.getStatut() != com.example.gestionimmobilier.models.enums.StatutBien.DISPONIBLE) {
            throw new ValidationException(ErrorMessages.BIEN_NON_DISPONIBLE_POUR_LOCATION);
        }
        if (req.dateFin().isBefore(req.dateDebut())) {
            throw new ValidationException("La date de fin doit être postérieure à la date de début.");
        }
        if (bailRepository.existsByBien_IdAndStatutIn(bienId, STATUTS_BLOQUANTS_NOUVELLE_DEMANDE)) {
            throw new ValidationException(ErrorMessages.BIEN_DEJA_LIE_CONTRAT);
        }

        Proprietaire proprietaire = bien.getProprietaire();
        MandatDeGestion mandatActif = mandatRepository.findByBien_IdAndStatut(bienId, StatutMandat.ACTIF).orElse(null);

        StatutBail statutInitial;
        Agent agentDemande = null;
        if (mandatActif != null) {
            if (mandatActif.getAgent() == null) {
                throw new ValidationException(ErrorMessages.MANDAT_ACTIF_SANS_AGENT);
            }
            agentDemande = mandatActif.getAgent();
            statutInitial = StatutBail.EN_ATTENTE_VALIDATION_AGENT;
        } else {
            statutInitial = StatutBail.EN_ATTENTE;
        }

        String numContrat = "DEMANDE-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Bail bail = Bail.builder()
                .numContrat(numContrat)
                .dateSignature(LocalDate.now())
                .documentUrl(null)
                .client(client)
                .proprietaire(proprietaire)
                .agent(agentDemande)
                .bien(bien)
                .dateDebut(req.dateDebut())
                .dateFin(req.dateFin())
                .loyerHC(bien.getPrixBase())
                .charges(BigDecimal.ZERO)
                .statut(statutInitial)
                .build();

        Bail saved = bailRepository.save(bail);
        return new ContratResponse(
                saved.getId(),
                saved.getNumContrat(),
                saved.getDateSignature(),
                saved.getDateDebut(),
                saved.getDateFin(),
                saved.getLoyerHC(),
                saved.getCharges(),
                saved.getDocumentUrl(),
                saved.getStatut(),
                bienMapper.toBienResponse(saved.getBien()),
                userMapper.toProprietaireResponse(saved.getProprietaire()),
                userMapper.toLocataireResponse(saved.getClient()),
                saved.getAgent() != null ? saved.getAgent().getId() : null,
                saved.getAgent() != null ? (saved.getAgent().getFirstName() + " " + saved.getAgent().getLastName()).trim() : null
        );
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> listerMesDemandes(String keycloakId) {
        getClientByKeycloakId(keycloakId);
        return contratService.listerContratsParLocataireKeycloak(keycloakId);
    }

    private Client getClientByKeycloakId(String keycloakId) {
        Utilisateur u = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CLIENT_INTROUVABLE));
        if (!(u instanceof Client client) || !u.getRoles().contains(Role.ROLE_CLIENT)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        return client;
    }
}

