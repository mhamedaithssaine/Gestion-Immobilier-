package com.example.gestionimmobilier.service;

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
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.repository.AdresseRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class BienService {

    private final UtilisateurRepository utilisateurRepository;
    private final AdresseRepository adresseRepository;
    private final BienImmobilierRepository bienImmobilierRepository;
    private final FileStorageService fileStorageService;
    private final BienMapper bienMapper;

    public BienService(UtilisateurRepository utilisateurRepository,AdresseRepository adresseRepository,
                       BienImmobilierRepository bienImmobilierRepository,
                       FileStorageService fileStorageService,
                       BienMapper bienMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.adresseRepository = adresseRepository;
        this.bienImmobilierRepository = bienImmobilierRepository;
        this.fileStorageService = fileStorageService;
        this.bienMapper = bienMapper;
    }

    @Transactional
    public BienResponse creerBien(String keycloakId, CreateBienRequest request, MultipartFile[] images) {
        Proprietaire proprietaire = getProprietaireByKeycloakId(keycloakId);

        Adresse adresse = mapAdresse(request.getAdresse());
        adresse = adresseRepository.save(adresse);

        String reference = genererReferenceUnique();

        BienImmobilier bien = request.toEntity(adresse, proprietaire, reference, keycloakId);
        bien = bienImmobilierRepository.save(bien);

        if (images != null && images.length > 0) {
            List<String> paths = fileStorageService.storeBienImages(bien.getId(), images);
            bien.setImages(paths);
            bien = bienImmobilierRepository.save(bien);
        }

        return bienMapper.toBienResponse(bien);
    }

    @Transactional
    public BienResponse modifierBien(UUID bienId, String keycloakId, CreateBienRequest request, MultipartFile[] images) {
        Proprietaire proprietaire = getProprietaireByKeycloakId(keycloakId);

        BienImmobilier bien = bienImmobilierRepository
                .findByIdAndProprietaire(bienId, proprietaire)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

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
            List<String> paths = fileStorageService.storeBienImages(bien.getId(), images);
            bien.setImages(paths);
        }

        bien = bienImmobilierRepository.save(bien);
        return bienMapper.toBienResponse(bien);
    }

    @Transactional
    public void supprimerBien(UUID bienId, String keycloakId) {
        Proprietaire proprietaire = getProprietaireByKeycloakId(keycloakId);

        BienImmobilier bien = bienImmobilierRepository
                .findByIdAndProprietaire(bienId, proprietaire)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BIEN_INTROUVABLE));

        bienImmobilierRepository.delete(bien);
    }

    public List<BienResponse> listerBiens(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new ValidationException(ErrorMessages.IDENTITE_PROPRIETAIRE_REQUISE);
        }
        Proprietaire proprietaire = getProprietaireByKeycloakId(keycloakId);
        List<BienImmobilier> biens = bienImmobilierRepository.findByProprietaireOrderByCreatedAtDesc(proprietaire);
        return biens.stream().map(bienMapper::toBienResponse).toList();
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

}
