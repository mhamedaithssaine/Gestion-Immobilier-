package com.example.gestionimmobilier.service.contrat;

import com.example.gestionimmobilier.dto.contrat.ContratResponse;
import com.example.gestionimmobilier.dto.contrat.CreateContratRequest;
import com.example.gestionimmobilier.dto.contrat.UpdateContratRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.user.LocataireResponse;
import com.example.gestionimmobilier.dto.user.ProprietaireResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContratService {

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

        if (bailRepository.existsByBien_IdAndStatutIn(bien.getId(), List.of(StatutBail.ACTIF, StatutBail.EN_ATTENTE))) {
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
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (bail.getStatut() == StatutBail.RESILIE || bail.getStatut() == StatutBail.TERMINE) {
            throw new ValidationException(ErrorMessages.CONTRAT_NON_MODIFIABLE);
        }
        if (request.dateDebut() != null) bail.setDateDebut(request.dateDebut());
        if (request.dateFin() != null) bail.setDateFin(request.dateFin());
        if (request.loyerHC() != null) bail.setLoyerHC(request.loyerHC());
        if (request.charges() != null) bail.setCharges(request.charges());
        if (request.dateSignature() != null) bail.setDateSignature(request.dateSignature());
        if (request.documentUrl() != null) bail.setDocumentUrl(request.documentUrl());
        bail = bailRepository.save(bail);
        return toContratResponse(bail);
    }

    @Transactional
    public ContratResponse resilierContrat(UUID id) {
        Bail bail = bailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));
        if (bail.getStatut() == StatutBail.RESILIE || bail.getStatut() == StatutBail.TERMINE) {
            throw new ValidationException(ErrorMessages.CONTRAT_DEJA_RESILIE);
        }
        bail.setStatut(StatutBail.RESILIE);
        bail = bailRepository.save(bail);
        BienImmobilier bien = bail.getBien();
        bien.setStatut(StatutBien.DISPONIBLE);
        bienRepository.save(bien);
        return toContratResponse(bail);
    }

    public List<ContratResponse> listerContratsParLocataire(UUID locataireId) {
        List<Bail> baux = bailRepository.findByClient_IdOrderByDateDebutDesc(locataireId);
        return baux.stream().map(this::toContratResponse).toList();
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

