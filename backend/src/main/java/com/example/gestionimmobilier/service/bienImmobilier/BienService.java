package com.example.gestionimmobilier.service.bienImmobilier;

import com.example.gestionimmobilier.dto.immobilier.AdresseRequest;
import com.example.gestionimmobilier.dto.immobilier.BienResponse;
import com.example.gestionimmobilier.dto.immobilier.CreateAppartementRequest;
import com.example.gestionimmobilier.dto.immobilier.CreateBienRequest;
import com.example.gestionimmobilier.dto.immobilier.CreateMaisonRequest;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ForbiddenException;
import com.example.gestionimmobilier.exception.InternalServerException;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.mapper.BienMapper;
import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.immobilier.Appartement;
import com.example.gestionimmobilier.models.entity.immobilier.BienImmobilier;
import com.example.gestionimmobilier.models.entity.immobilier.Maison;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.Role;
import com.example.gestionimmobilier.repository.AdresseRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.service.storage.CloudStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BienService {

    private final UtilisateurRepository utilisateurRepository;
    private final AdresseRepository adresseRepository;
    private final BienImmobilierRepository bienImmobilierRepository;
    private final CloudStorageService cloudStorageService;
    private final BienMapper bienMapper;

    public BienService(UtilisateurRepository utilisateurRepository,AdresseRepository adresseRepository,
                       BienImmobilierRepository bienImmobilierRepository,
                       CloudStorageService cloudStorageService,
                       BienMapper bienMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.adresseRepository = adresseRepository;
        this.bienImmobilierRepository = bienImmobilierRepository;
        this.cloudStorageService = cloudStorageService;
        this.bienMapper = bienMapper;
    }

    @Transactional
    public BienResponse creerBien(String keycloakId, CreateBienRequest request, MultipartFile[] images) {
        requireProprietaireRole(keycloakId);
        Proprietaire proprietaire = getProprietaireByKeycloakId(keycloakId);

        Adresse adresse = mapAdresse(request.getAdresse());
        adresse = adresseRepository.save(adresse);

        String reference = genererReferenceUnique();

        BienImmobilier bien = request.toEntity(adresse, proprietaire, reference, keycloakId);
        bien = bienImmobilierRepository.save(bien);

        if (images != null && images.length > 0) {
            List<String> urls = cloudStorageService.uploadBienImages(bien.getId(), images);
            bien.setImages(urls);
            bien = bienImmobilierRepository.save(bien);
        }

        return bienMapper.toBienResponse(bien);
    }

    @Transactional
    public BienResponse modifierBien(UUID bienId, String keycloakId, CreateBienRequest request, MultipartFile[] images) {
        Utilisateur utilisateur = getUtilisateurByKeycloakId(keycloakId);

        if (hasRole(utilisateur, Role.ROLE_ADMIN)) {
            BienImmobilier existing = bienImmobilierRepository.findById(bienId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
            return modifierBien(bienId, existing.getProprietaire().getKeycloakId(), request, images);
        }

        requireProprietaireRole(utilisateur);
        Proprietaire proprietaire = (Proprietaire) utilisateur;

        BienImmobilier bien = bienImmobilierRepository
                .findByIdAndProprietaire(bienId, proprietaire)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        applyBienUpdates(bien, request, images);
        bien = bienImmobilierRepository.save(bien);
        return bienMapper.toBienResponse(bien);
    }

    /**
     * Mise à jour d’un bien par l’agent mandataire (même contenu que {@link #modifierBien}, sans changement de propriétaire).
     */
    @Transactional
    public BienResponse modifierBienPourAgent(UUID bienId, String keycloakId, CreateBienRequest request, MultipartFile[] images) {
        Utilisateur u = getUtilisateurByKeycloakId(keycloakId);
        if (!hasRole(u, Role.ROLE_AGENT) || !(u instanceof Agent agent)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        if (!bienImmobilierRepository.existsAgentAccessToBien(agent.getId(), bienId)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        BienImmobilier bien = bienImmobilierRepository.findById(bienId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
        applyBienUpdates(bien, request, images);
        bien = bienImmobilierRepository.save(bien);
        return bienMapper.toBienResponse(bien);
    }

    private void applyBienUpdates(BienImmobilier bien, CreateBienRequest request, MultipartFile[] images) {
        bien.setTitre(request.getTitre());
        bien.setSurface(request.getSurface());
        bien.setPrixBase(request.getPrixBase());
        bien.setStatut(request.getStatut());

        AdresseRequest adrReq = request.getAdresse();
        if (bien.getAdresse() == null) {
            Adresse adresse = mapAdresse(adrReq);
            adresse = adresseRepository.save(adresse);
            bien.setAdresse(adresse);
        } else {
            bien.getAdresse().setRue(adrReq.rue());
            bien.getAdresse().setVille(adrReq.ville());
            bien.getAdresse().setCodePostal(adrReq.codePostal());
            bien.getAdresse().setPays(adrReq.pays());
        }

        if (bien instanceof Appartement appartement && request instanceof CreateAppartementRequest reqApp) {
            appartement.setEtage(reqApp.getEtage());
            appartement.setAscenseur(reqApp.isAscenseur());
        } else if (bien instanceof Maison maison && request instanceof CreateMaisonRequest reqMaison) {
            Double terrain = reqMaison.getSurfaceTerrain() != null ? reqMaison.getSurfaceTerrain() : 0.0;
            maison.setSurfaceTerrain(terrain);
            maison.setGarage(reqMaison.isGarage());
        } else {
            throw new ValidationException(ErrorMessages.BIEN_TYPE_INCOMPATIBLE);
        }

        if (images != null && images.length > 0) {
            List<String> newUrls = cloudStorageService.uploadBienImages(bien.getId(), images);
            List<String> merged = new ArrayList<>();
            if (bien.getImages() != null) {
                merged.addAll(bien.getImages());
            }
            merged.addAll(newUrls);
            bien.setImages(merged);
        }
    }

    @Transactional
    public void supprimerBien(UUID bienId, String keycloakId) {
        Utilisateur utilisateur = getUtilisateurByKeycloakId(keycloakId);

        if (hasRole(utilisateur, Role.ROLE_ADMIN)) {
            BienImmobilier bien = bienImmobilierRepository.findById(bienId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
            bienImmobilierRepository.delete(bien);
            return;
        }

        requireProprietaireRole(utilisateur);
        Proprietaire proprietaire = (Proprietaire) utilisateur;

        BienImmobilier bien = bienImmobilierRepository
                .findByIdAndProprietaire(bienId, proprietaire)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        bienImmobilierRepository.delete(bien);
    }

    public BienResponse getBienById(UUID bienId, String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new ValidationException(ErrorMessages.IDENTITE_PROPRIETAIRE_REQUISE);
        }
        Utilisateur utilisateur = getUtilisateurByKeycloakId(keycloakId);
        BienImmobilier bien;
        if (hasRole(utilisateur, Role.ROLE_ADMIN)) {
            bien = bienImmobilierRepository.findById(bienId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
        } else if (hasRole(utilisateur, Role.ROLE_AGENT)) {
            Agent agent = getAgentByUtilisateur(utilisateur);
            if (!bienImmobilierRepository.existsAgentAccessToBien(agent.getId(), bienId)) {
                throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
            }
            bien = bienImmobilierRepository.findById(bienId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
        } else if (hasRole(utilisateur, Role.ROLE_PROPRIETAIRE)) {
            Proprietaire proprietaire = (Proprietaire) utilisateur;
            bien = bienImmobilierRepository.findByIdAndProprietaire(bienId, proprietaire)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));
        } else {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        return bienMapper.toBienResponse(bien);
    }

    public List<BienResponse> listerBiens(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new ValidationException(ErrorMessages.IDENTITE_PROPRIETAIRE_REQUISE);
        }
        Utilisateur utilisateur = getUtilisateurByKeycloakId(keycloakId);
        List<BienImmobilier> biens;
        if (hasRole(utilisateur, Role.ROLE_ADMIN)) {
            biens = bienImmobilierRepository.findAllByOrderByCreatedAtDesc();
        } else if (hasRole(utilisateur, Role.ROLE_AGENT)) {
            Agent agent = getAgentByUtilisateur(utilisateur);
            biens = bienImmobilierRepository.findDistinctByAgentMandat(agent.getId());
        } else if (hasRole(utilisateur, Role.ROLE_PROPRIETAIRE)) {
            Proprietaire proprietaire = (Proprietaire) utilisateur;
            biens = bienImmobilierRepository.findByProprietaireOrderByCreatedAtDesc(proprietaire);
        } else {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        return biens.stream().map(bienMapper::toBienResponse).toList();
    }

    private Utilisateur getUtilisateurByKeycloakId(String keycloakId) {
        return utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
    }

    private static boolean hasRole(Utilisateur utilisateur, Role role) {
        return utilisateur.getRoles() != null && utilisateur.getRoles().contains(role);
    }

    private void requireProprietaireRole(String keycloakId) {
        requireProprietaireRole(getUtilisateurByKeycloakId(keycloakId));
    }

    private static void requireProprietaireRole(Utilisateur utilisateur) {
        if (!hasRole(utilisateur, Role.ROLE_PROPRIETAIRE)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
    }

    private static Agent getAgentByUtilisateur(Utilisateur utilisateur) {
        if (!(utilisateur instanceof Agent agent)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        return agent;
    }

    private Proprietaire getProprietaireByKeycloakId(String keycloakId) {
        Utilisateur u = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE));
        if (!(u instanceof Proprietaire)) {
            throw new ForbiddenException(ErrorMessages.ACCES_REFUSE);
        }
        return (Proprietaire) u;
    }

    private Adresse mapAdresse(AdresseRequest req) {
        return Adresse.builder()
                .rue(req.rue())
                .ville(req.ville())
                .codePostal(req.codePostal())
                .pays(req.pays())
                .build();
    }

    private String genererReferenceUnique() {
        String ref;
        int attempts = 0;
        do {
            ref = "BIEN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (++attempts > 10) {
                throw new InternalServerException(ErrorMessages.REFERENCE_UNIQUE_IMPOSSIBLE);
            }
        } while (bienImmobilierRepository.existsByReference(ref));
        return ref;
    }

    @Transactional
    public BienResponse associerBienAProprietaire(UUID bienId, UUID proprietaireId) {
        BienImmobilier bien = bienImmobilierRepository.findById(bienId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        Utilisateur utilisateur = utilisateurRepository.findById(proprietaireId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE));

        if (!(utilisateur instanceof Proprietaire proprietaire)) {
            throw new ValidationException(ErrorMessages.PROPRIETAIRE_INTROUVABLE);
        }

        bien.setProprietaire(proprietaire);
        bien = bienImmobilierRepository.save(bien);
        return bienMapper.toBienResponse(bien);
    }
}
