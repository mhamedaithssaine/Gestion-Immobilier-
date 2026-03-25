package com.example.gestionimmobilier.service.mandat;

import com.example.gestionimmobilier.dto.contrat.CreateMandatRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MandatService {

    private static final Logger log = LoggerFactory.getLogger(MandatService.class);

    private final MandatDeGestionRepository mandatRepository;
    private final BienImmobilierRepository bienRepository;
    private final UtilisateurRepository utilisateurRepository;

    public MandatService(MandatDeGestionRepository mandatRepository,
                         BienImmobilierRepository bienRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.mandatRepository = mandatRepository;
        this.bienRepository = bienRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public MandatResponse creerMandat(CreateMandatRequest request) {
        log.info("Création mandat bienId={} proprietaireId={} agentId={} commissionPct={}",
                request.bienId(), request.proprietaireId(), request.agentId(), request.commissionPct());
        BienImmobilier bien = bienRepository.findById(request.bienId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        Utilisateur uProprio = utilisateurRepository.findById(request.proprietaireId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE));
        if (!(uProprio instanceof Proprietaire proprietaire)) {
            throw new ValidationException(ErrorMessages.PROPRIETAIRE_INTROUVABLE);
        }

        Utilisateur uAgent = utilisateurRepository.findById(request.agentId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (!(uAgent instanceof Agent agent)) {
            throw new ValidationException(ErrorMessages.UTILISATEUR_N_EST_PAS_AGENT);
        }
        if (!agent.isEnabled()) {
            throw new ValidationException(ErrorMessages.AGENT_NON_ACTIVE_POUR_MANDAT);
        }

        if (!bien.getProprietaire().getId().equals(proprietaire.getId())) {
            throw new ValidationException(ErrorMessages.BIEN_N_APPARTIENT_PAS_PROPRIETAIRE);
        }

        mandatRepository.findByBien_IdAndStatut(request.bienId(), StatutMandat.ACTIF)
                .ifPresent(m -> {
                    throw new ValidationException(ErrorMessages.BIEN_DEJA_SOUS_MANDAT_ACTIF);
                });

        String numMandat = "MANDAT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        MandatDeGestion mandat = MandatDeGestion.builder()
                .numMandat(numMandat)
                .proprietaire(proprietaire)
                .agent(agent)
                .bien(bien)
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .commissionPct(request.commissionPct())
                .statut(StatutMandat.ACTIF)
                .dateSignature(request.dateSignature() != null ? request.dateSignature() : LocalDate.now())
                .documentUrl(request.documentUrl())
                .build();

        mandat = mandatRepository.save(mandat);
        log.info("Mandat créé id={} numMandat={} statut={}", mandat.getId(), mandat.getNumMandat(), mandat.getStatut());
        return toMandatResponse(mandat);
    }

    public MandatResponse getMandatById(UUID id) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        return toMandatResponse(mandat);
    }

    public List<MandatResponse> listerMandats(StatutMandat statut, UUID proprietaireId, UUID bienId) {
        if (bienId != null) {
            return mandatRepository.findByBien_IdOrderByDateDebutDesc(bienId).stream()
                    .map(this::toMandatResponse).toList();
        }
        if (proprietaireId != null) {
            return mandatRepository.findByProprietaire_IdOrderByDateDebutDesc(proprietaireId).stream()
                    .map(this::toMandatResponse).toList();
        }
        List<MandatDeGestion> mandats = statut == null
                ? mandatRepository.findAllByOrderByDateDebutDesc()
                : mandatRepository.findByStatutOrderByDateDebutDesc(statut);
        return mandats.stream().map(this::toMandatResponse).toList();
    }

    @Transactional
    public MandatResponse resilierMandat(UUID id) {
        log.info("Résiliation mandat {}", id);
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (mandat.getStatut() == StatutMandat.RESILIE || mandat.getStatut() == StatutMandat.TERMINE) {
            throw new ValidationException(ErrorMessages.MANDAT_DEJA_RESILIE);
        }
        mandat.setStatut(StatutMandat.RESILIE);
        mandat = mandatRepository.save(mandat);
        log.info("Mandat résilié id={} nouveauStatut={}", mandat.getId(), mandat.getStatut());
        return toMandatResponse(mandat);
    }

    private MandatResponse toMandatResponse(MandatDeGestion m) {
        String proprioNom = (m.getProprietaire().getFirstName() + " " + m.getProprietaire().getLastName()).trim();
        String agentNom = (m.getAgent().getFirstName() + " " + m.getAgent().getLastName()).trim();
        return new MandatResponse(
                m.getId(),
                m.getNumMandat(),
                m.getProprietaire().getId(),
                proprioNom,
                m.getAgent().getId(),
                agentNom,
                m.getBien().getId(),
                m.getBien().getReference(),
                m.getDateDebut(),
                m.getDateFin(),
                m.getCommissionPct(),
                m.getStatut(),
                m.getDateSignature(),
                m.getDocumentUrl()
        );
    }
}
