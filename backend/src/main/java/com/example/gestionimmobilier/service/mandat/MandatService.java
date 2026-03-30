package com.example.gestionimmobilier.service.mandat;

import com.example.gestionimmobilier.dto.contrat.CreateMandatRequest;
import com.example.gestionimmobilier.dto.contrat.MandatResponse;
import com.example.gestionimmobilier.dto.contrat.UpdateMandatRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ForbiddenException;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.BienMapper;
import com.example.gestionimmobilier.models.entity.contrat.MandatDeGestion;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.repository.AgentRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.storage.CloudStorageService;
import com.example.gestionimmobilier.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MandatService {

    private static final Logger log = LoggerFactory.getLogger(MandatService.class);

    private final MandatDeGestionRepository mandatRepository;
    private final BienImmobilierRepository bienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;
    private final CloudStorageService cloudStorageService;
    private final FileStorageService fileStorageService;
    private final BienMapper bienMapper;

    public MandatService(MandatDeGestionRepository mandatRepository,
                         BienImmobilierRepository bienRepository,
                         UtilisateurRepository utilisateurRepository,
                         AgentRepository agentRepository,
                         CloudStorageService cloudStorageService,
                         FileStorageService fileStorageService,
                         BienMapper bienMapper) {
        this.mandatRepository = mandatRepository;
        this.bienRepository = bienRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.agentRepository = agentRepository;
        this.cloudStorageService = cloudStorageService;
        this.fileStorageService = fileStorageService;
        this.bienMapper = bienMapper;
    }

    @Transactional
    public MandatResponse creerMandat(CreateMandatRequest request) {
        return creerMandat(request, null);
    }

    @Transactional
    public MandatResponse creerMandat(CreateMandatRequest request, MultipartFile document) {
        log.info("Création mandat bienId={} proprietaireId={} agentId={} commissionPct={}",
                request.bienId(), request.proprietaireId(), request.agentId(), request.commissionPct());

        validateDates(request);
        validateDocument(document);

        BienImmobilier bien = bienRepository.findById(request.bienId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        assertPasDeMandatConflitantSurBien(request.bienId());

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
        if (document != null && !document.isEmpty()) {
            mandat.setDocumentUrl(cloudStorageService.uploadMandatDocument(mandat.getId(), document));
            mandat = mandatRepository.save(mandat);
        }
        log.info("Mandat créé id={} numMandat={} statut={}", mandat.getId(), mandat.getNumMandat(), mandat.getStatut());
        return toMandatResponse(mandat);
    }

    private void assertPasDeMandatConflitantSurBien(UUID bienId) {
        boolean conflict = mandatRepository.findByBien_IdOrderByDateDebutDesc(bienId).stream()
                .anyMatch(m -> m.getStatut() == StatutMandat.ACTIF
                        || m.getStatut() == StatutMandat.EN_ATTENTE_RESILIATION);
        if (conflict) {
            throw new ValidationException(ErrorMessages.BIEN_DEJA_SOUS_MANDAT_ACTIF);
        }
    }

    public MandatResponse getMandatById(UUID id) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        return toMandatResponse(mandat);
    }

    public MandatResponse getMandatByIdPourProprietaire(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (!mandat.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return toMandatResponse(mandat);
    }

    public MandatResponse getMandatByIdPourAgent(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        Agent agent = agentRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (!mandat.getAgent().getId().equals(agent.getId())) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
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

    public List<MandatResponse> listerMandatsPourAgent(String keycloakId, StatutMandat statut) {
        Agent agent = agentRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        List<MandatDeGestion> list = mandatRepository.findByAgent_IdOrderByDateDebutDesc(agent.getId());
        if (statut != null) {
            return list.stream().filter(m -> m.getStatut() == statut).map(this::toMandatResponse).toList();
        }
        return list.stream().map(this::toMandatResponse).toList();
    }

    public List<MandatResponse> listerMandatsParProprietaireKeycloak(String keycloakId) {
        return mandatRepository.findByProprietaire_KeycloakIdOrderByDateDebutDesc(keycloakId)
                .stream()
                .map(this::toMandatResponse)
                .toList();
    }

    public List<BienResponse> listerBiensParProprietaire(UUID proprietaireId) {
        return bienRepository.findByProprietaire_IdOrderByCreatedAtDesc(proprietaireId)
                .stream()
                .map(bienMapper::toBienResponse)
                .toList();
    }

    public List<Utilisateur> listerAgentsEligiblesPourCreate(String keycloakId) {
        Utilisateur me = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (me.getRoles() != null && me.getRoles().contains(Role.ROLE_ADMIN)) {
            return agentRepository.findByEnabledTrueOrderByFirstNameAscLastNameAsc().stream().map(a -> (Utilisateur) a).toList();
        }
        if (!(me instanceof Agent agentMe)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        if (agentMe.getAgence() != null) {
            return agentRepository.findByAgence_IdAndEnabledTrueOrderByFirstNameAscLastNameAsc(agentMe.getAgence().getId())
                    .stream().map(a -> (Utilisateur) a).toList();
        }
        return List.of(agentMe);
    }

    @Transactional
    public MandatResponse demanderResiliationProprietaire(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (!mandat.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return appliquerDemandeResiliation(mandat);
    }

    @Transactional
    public MandatResponse demanderResiliationAgent(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        Agent agent = agentRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (!mandat.getAgent().getId().equals(agent.getId())) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return appliquerDemandeResiliation(mandat);
    }

    private MandatResponse appliquerDemandeResiliation(MandatDeGestion mandat) {
        assertPeutDemanderResiliation(mandat);
        mandat.setStatut(StatutMandat.EN_ATTENTE_RESILIATION);
        mandat = mandatRepository.save(mandat);
        log.info("Demande résiliation mandat id={} statut={}", mandat.getId(), mandat.getStatut());
        return toMandatResponse(mandat);
    }

    private static void assertPeutDemanderResiliation(MandatDeGestion mandat) {
        if (mandat.getStatut() == StatutMandat.EN_ATTENTE_RESILIATION) {
            throw new ValidationException(ErrorMessages.MANDAT_RESILIATION_DEJA_EN_COURS);
        }
        if (mandat.getStatut() != StatutMandat.ACTIF) {
            throw new ValidationException(ErrorMessages.MANDAT_RESILIATION_IMPOSSIBLE_STATUT);
        }
    }

    @Transactional
    public MandatResponse approuverResiliationAdmin(UUID id) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (mandat.getStatut() != StatutMandat.EN_ATTENTE_RESILIATION) {
            throw new ValidationException(ErrorMessages.MANDAT_APPROBATION_RESILIATION_IMPOSSIBLE);
        }
        mandat.setStatut(StatutMandat.RESILIE);
        mandat = mandatRepository.save(mandat);
        log.info("Résiliation approuvée mandat id={}", id);
        return toMandatResponse(mandat);
    }

    @Transactional
    public MandatResponse rejeterDemandeResiliationAdmin(UUID id) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (mandat.getStatut() != StatutMandat.EN_ATTENTE_RESILIATION) {
            throw new ValidationException(ErrorMessages.MANDAT_APPROBATION_RESILIATION_IMPOSSIBLE);
        }
        mandat.setStatut(StatutMandat.ACTIF);
        mandat = mandatRepository.save(mandat);
        log.info("Demande résiliation rejetée mandat id={}", id);
        return toMandatResponse(mandat);
    }

    /**
     * @param document optionnel ; si présent, remplace le PDF (upload Cloudinary).
     */
    @Transactional
    public MandatResponse mettreAJourMandatAdmin(UUID id, UpdateMandatRequest req, MultipartFile document) {
        validateDocument(document);
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (req.dateDebut() != null) {
            mandat.setDateDebut(req.dateDebut());
        }
        if (req.dateFin() != null) {
            mandat.setDateFin(req.dateFin());
        }
        if (req.commissionPct() != null) {
            mandat.setCommissionPct(req.commissionPct());
        }
        if (req.dateSignature() != null) {
            mandat.setDateSignature(req.dateSignature());
        }
        LocalDate now = LocalDate.now();
        if (!mandat.getDateFin().isAfter(mandat.getDateDebut())) {
            throw new ValidationException(ErrorMessages.MANDAT_DATE_FIN_AVANT_DEBUT);
        }
        if (mandat.getDateSignature() != null && mandat.getDateSignature().isAfter(now)) {
            throw new ValidationException(ErrorMessages.MANDAT_DATE_SIGNATURE_FUTURE);
        }
        if (document != null && !document.isEmpty()) {
            mandat.setDocumentUrl(cloudStorageService.uploadMandatDocument(mandat.getId(), document));
        }
        mandat = mandatRepository.save(mandat);
        log.info("Mandat mis à jour id={}", id);
        return toMandatResponse(mandat);
    }

    public record MandatDocumentFile(byte[] content, String filename) {}

    public MandatDocumentFile getMandatDocumentForAdmin(UUID id) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        return buildMandatDocumentFile(mandat);
    }

    public MandatDocumentFile getMandatDocumentForAgent(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        Agent agent = agentRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (!mandat.getAgent().getId().equals(agent.getId())) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return buildMandatDocumentFile(mandat);
    }

    public MandatDocumentFile getMandatDocumentForProprietaire(UUID id, String keycloakId) {
        MandatDeGestion mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.MANDAT_INTROUVABLE));
        if (!mandat.getProprietaire().getKeycloakId().equals(keycloakId)) {
            throw new ForbiddenException(ErrorMessages.MESSAGE_FORBIDDEN);
        }
        return buildMandatDocumentFile(mandat);
    }

    private MandatDocumentFile buildMandatDocumentFile(MandatDeGestion mandat) {
        String url = mandat.getDocumentUrl();
        if (url == null || url.isBlank()) {
            throw new ValidationException(ErrorMessages.MANDAT_SANS_DOCUMENT);
        }
        byte[] bytes = fileStorageService.loadMandatDocumentIfPresent(mandat.getId());
        if (bytes != null && bytes.length > 0) {
            log.debug("Document mandat id={} servi depuis copie locale", mandat.getId());
            return new MandatDocumentFile(bytes, sanitizeMandatDocumentFilename(mandat.getNumMandat()));
        }
        bytes = cloudStorageService.fetchDeliveryBytes(url);
        return new MandatDocumentFile(bytes, sanitizeMandatDocumentFilename(mandat.getNumMandat()));
    }

    private static String sanitizeMandatDocumentFilename(String numMandat) {
        String base = numMandat == null ? "mandat" : numMandat.replaceAll("[^a-zA-Z0-9_.-]", "_");
        if (base.isBlank()) {
            base = "mandat";
        }
        if (!base.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            base = base + ".pdf";
        }
        return base;
    }

    @Transactional
    public MandatResponse resilierMandat(UUID id) {
        log.info("Résiliation mandat (admin) {}", id);
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

    private static void validateDates(CreateMandatRequest request) {
        LocalDate now = LocalDate.now();
        if (request.dateDebut().isBefore(now)) {
            throw new ValidationException(ErrorMessages.MANDAT_DATE_DEBUT_PASSEE);
        }
        if (!request.dateFin().isAfter(request.dateDebut())) {
            throw new ValidationException(ErrorMessages.MANDAT_DATE_FIN_AVANT_DEBUT);
        }
        if (request.dateSignature() != null && request.dateSignature().isAfter(now)) {
            throw new ValidationException(ErrorMessages.MANDAT_DATE_SIGNATURE_FUTURE);
        }
    }

    private static void validateDocument(MultipartFile document) {
        if (document == null || document.isEmpty()) return;
        String contentType = document.getContentType();
        String originalName = document.getOriginalFilename();
        boolean pdfMime = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean pdfName = originalName != null && originalName.toLowerCase().endsWith(".pdf");
        if (!pdfMime && !pdfName) {
            throw new ValidationException(ErrorMessages.MANDAT_DOCUMENT_PDF_REQUIS);
        }
    }
}
