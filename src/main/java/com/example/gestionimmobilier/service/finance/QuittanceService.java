package com.example.gestionimmobilier.service.finance;

import com.example.gestionimmobilier.dto.finance.QuittancePdfData;
import com.example.gestionimmobilier.dto.finance.QuittanceResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ValidationException;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.immobilier.Adresse;
import com.example.gestionimmobilier.models.entity.finance.Quittance;
import com.example.gestionimmobilier.models.entity.finance.Versement;
import com.example.gestionimmobilier.models.entity.user.Client;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.repository.QuittanceRepository;
import com.example.gestionimmobilier.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

@Service
public class QuittanceService {

    private static final String REF_PREFIX = "QUIT-";

    private final QuittanceRepository quittanceRepository;
    private final PdfGenerationService pdfGenerationService;
    private final FileStorageService fileStorageService;

    public QuittanceService(QuittanceRepository quittanceRepository,
                            PdfGenerationService pdfGenerationService,
                            FileStorageService fileStorageService) {
        this.quittanceRepository = quittanceRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Generates a Quittance from an existing Versement, creates the PDF, stores it, and saves the Quittance with the PDF URL.
     */
    @Transactional
    public Quittance generateFromVersement(Versement versement) {
        if (quittanceRepository.findByVersement_Id(versement.getId()).isPresent()) {
            throw new ValidationException(ErrorMessages.QUITTANCE_DEJA_GENEREE);
        }

        YearMonth ym = YearMonth.from(versement.getDateVersement());
        String referenceQuittance = REF_PREFIX + versement.getId().toString().substring(0, 8).toUpperCase() + "-" + ym.getYear() + String.format("%02d", ym.getMonthValue());

        Quittance quittance = Quittance.builder()
                .referenceQuittance(referenceQuittance)
                .mois(ym.getMonthValue())
                .annee(ym.getYear())
                .dateGeneration(LocalDateTime.now())
                .versement(versement)
                .proprietaire(versement.getProprietaire())
                .build();
        quittance = quittanceRepository.save(quittance);

        QuittancePdfData pdfData = buildPdfData(versement, quittance);
        byte[] pdfBytes = pdfGenerationService.generateQuittancePdf(pdfData);
        String relativePath = fileStorageService.storeQuittancePdf(quittance.getId(), pdfBytes);
        String urlPdf = "/uploads/" + relativePath;
        quittance.setUrlPdf(urlPdf);
        quittance = quittanceRepository.save(quittance);

        return quittance;
    }

    public QuittanceResponse toResponse(Quittance q) {
        return new QuittanceResponse(
                q.getId(),
                q.getReferenceQuittance(),
                q.getMois(),
                q.getAnnee(),
                q.getUrlPdf(),
                q.getDateGeneration(),
                q.getVersement().getId(),
                q.getProprietaire().getId()
        );
    }

    private QuittancePdfData buildPdfData(Versement v, Quittance q) {
        Bail bail = v.getBail();
        Proprietaire proprio = v.getProprietaire();
        Client locataire = (Client) bail.getClient();

        String adresseBien = formatAdresse(bail.getBien().getAdresse());
        String proprioNom = (proprio.getFirstName() != null ? proprio.getFirstName() : "") + " " + (proprio.getLastName() != null ? proprio.getLastName() : "").trim();
        String proprioAdresse = proprio.getAdresseContact() != null ? proprio.getAdresseContact() : "";
        String locataireNom = (locataire.getFirstName() != null ? locataire.getFirstName() : "") + " " + (locataire.getLastName() != null ? locataire.getLastName() : "").trim();
        String locataireAdresse = "";

        BigDecimal loyerHC = bail.getLoyerHC();
        BigDecimal charges = bail.getCharges() != null ? bail.getCharges() : BigDecimal.ZERO;
        BigDecimal montantTotal = v.getMontant();

        String moisLibelle = Month.of(q.getMois()).getDisplayName(TextStyle.FULL, Locale.FRANCE);

        return new QuittancePdfData(
                q.getReferenceQuittance(),
                q.getMois(),
                q.getAnnee(),
                moisLibelle,
                adresseBien,
                proprioNom,
                proprioAdresse,
                locataireNom,
                locataireAdresse,
                bail.getNumContrat(),
                loyerHC,
                charges,
                montantTotal,
                v.getDateVersement(),
                v.getMode().name(),
                v.getReferencePaiement(),
                q.getDateGeneration()
        );
    }

    private String formatAdresse(Adresse a) {
        if (a == null) return "";
        return String.join(", ", Arrays.asList(
                a.getRue(),
                a.getCodePostal() + " " + a.getVille(),
                a.getPays()
        ));
    }

}
