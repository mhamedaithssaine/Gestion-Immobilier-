package com.example.gestionimmobilier.service.finance;

import com.example.gestionimmobilier.dto.finance.CreateVersementRequest;
import com.example.gestionimmobilier.dto.finance.VersementResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.finance.Quittance;
import com.example.gestionimmobilier.models.entity.finance.Versement;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.VersementRepository;
import com.example.gestionimmobilier.service.CloudStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VersementService {

    private final VersementRepository versementRepository;
    private final BailRepository bailRepository;
    private final QuittanceService quittanceService;
    private final CloudStorageService cloudStorageService;

    public VersementService(VersementRepository versementRepository,
                            BailRepository bailRepository,
                            QuittanceService quittanceService,
                            CloudStorageService cloudStorageService) {
        this.versementRepository = versementRepository;
        this.bailRepository = bailRepository;
        this.quittanceService = quittanceService;
        this.cloudStorageService = cloudStorageService;
    }

    /**
     * Creates a Versement, optionally stores payment proof file, then automatically generates a Quittance (with PDF).
     */
    @Transactional
    public VersementResponse createVersement(CreateVersementRequest request, MultipartFile preuvePaiement) {
        Bail bail = bailRepository.findById(request.bailId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.BAIL_INTROUVABLE));

        if (bail.getStatut() != StatutBail.ACTIF && bail.getStatut() != StatutBail.EN_ATTENTE) {
            throw new ValidationException(ErrorMessages.CONTRAT_ACTIF_REQUIS_VERSEMENT);
        }

        if (request.referencePaiement() != null && !request.referencePaiement().isBlank()
                && versementRepository.existsByReferencePaiement(request.referencePaiement())) {
            throw new ValidationException(ErrorMessages.VERSEMENT_REFERENCE_DEJA_UTILISEE);
        }

        LocalDateTime dateVersement = request.dateVersement() != null ? request.dateVersement() : LocalDateTime.now();

        Versement versement = Versement.builder()
                .dateVersement(dateVersement)
                .montant(request.montant())
                .mode(request.mode())
                .referencePaiement(request.referencePaiement())
                .preuvePaiementUrl(null)
                .valide(true)
                .bail(bail)
                .proprietaire(bail.getProprietaire())
                .build();
        versement = versementRepository.save(versement);

        if (preuvePaiement != null && !preuvePaiement.isEmpty()) {
            String url = cloudStorageService.uploadPreuveVersement(versement.getId(), preuvePaiement);
            if (url != null) {
                versement.setPreuvePaiementUrl(url);
                versement = versementRepository.save(versement);
            }
        }

        Quittance quittance = quittanceService.generateFromVersement(versement);

        return toResponse(versement, quittance);
    }

    private VersementResponse toResponse(Versement v, Quittance q) {
        return new VersementResponse(
                v.getId(),
                v.getBail().getId(),
                v.getBail().getNumContrat(),
                v.getDateVersement(),
                v.getMontant(),
                v.getMode(),
                v.getReferencePaiement(),
                v.getPreuvePaiementUrl(),
                v.isValide(),
                q != null ? q.getId() : null,
                q != null ? q.getReferenceQuittance() : null,
                q != null ? q.getUrlPdf() : null
        );
    }

    public List<VersementResponse> getVersementsByContrat(UUID contratId) {

        List<Versement> versements = versementRepository.findByBailId(contratId);

        return versements.stream()
                .map(v -> toResponse(v, null))
                .toList();
    }
}
