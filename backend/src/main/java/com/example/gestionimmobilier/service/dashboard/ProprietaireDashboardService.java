package com.example.gestionimmobilier.service.dashboard;

import com.example.gestionimmobilier.dto.dashboard.MoisMontantResponse;
import com.example.gestionimmobilier.dto.dashboard.ProprietaireDashboardOverviewResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.models.entity.user.Proprietaire;
import com.example.gestionimmobilier.models.entity.user.Utilisateur;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.UtilisateurRepository;
import com.example.gestionimmobilier.repository.VersementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProprietaireDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final BienImmobilierRepository bienRepository;
    private final BailRepository bailRepository;
    private final MandatDeGestionRepository mandatRepository;
    private final VersementRepository versementRepository;

    public ProprietaireDashboardService(
            UtilisateurRepository utilisateurRepository,
            BienImmobilierRepository bienRepository,
            BailRepository bailRepository,
            MandatDeGestionRepository mandatRepository,
            VersementRepository versementRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.bienRepository = bienRepository;
        this.bailRepository = bailRepository;
        this.mandatRepository = mandatRepository;
        this.versementRepository = versementRepository;
    }

    @Transactional(readOnly = true)
    public ProprietaireDashboardOverviewResponse getOverview(String keycloakId, int annee, int mois) {
        Utilisateur u = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        if (!(u instanceof Proprietaire)) {
            throw new ResourceNotFoundException(ErrorMessages.PROPRIETAIRE_INTROUVABLE);
        }

        long biensTotal = bienRepository.countByProprietaire_KeycloakId(keycloakId);
        long biensDispo = bienRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBien.DISPONIBLE);
        long biensLoues = bienRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBien.LOUE);
        long biensVendus = bienRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBien.VENDU);
        long biensCompromis =
                bienRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBien.SOUS_COMPROMIS);

        BigDecimal patrimoine = bienRepository.sumPrixBasePourProprietaireKeycloak(keycloakId);
        if (patrimoine == null) {
            patrimoine = BigDecimal.ZERO;
        }

        long contratsTotal = bailRepository.countByProprietaire_KeycloakId(keycloakId);
        long contratsActifs = bailRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBail.ACTIF);
        long enAttenteProp = bailRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutBail.EN_ATTENTE);
        long enAttenteAgent = bailRepository.countByProprietaire_KeycloakIdAndStatut(
                keycloakId, StatutBail.EN_ATTENTE_VALIDATION_AGENT);
        long contratsEnAttenteCandidatures = enAttenteProp + enAttenteAgent;

        long mandatsTotal = mandatRepository.countByProprietaire_KeycloakId(keycloakId);
        long mandatsActifs = mandatRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutMandat.ACTIF);
        long mandatsEnAttente =
                mandatRepository.countByProprietaire_KeycloakIdAndStatut(keycloakId, StatutMandat.EN_ATTENTE);

        BigDecimal loyerTheorique =
                bailRepository.sumLoyerEtChargesPourProprietaireStatut(keycloakId, StatutBail.ACTIF);
        if (loyerTheorique == null) {
            loyerTheorique = BigDecimal.ZERO;
        }

        YearMonth ym = YearMonth.of(annee, mois);
        var debutMois = ym.atDay(1).atStartOfDay();
        var finMois = ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000);
        BigDecimal revenusMois = versementRepository.sumMontantPourProprietaireEntre(keycloakId, debutMois, finMois);
        if (revenusMois == null) {
            revenusMois = BigDecimal.ZERO;
        }

        int tauxOcc = 0;
        if (biensTotal > 0) {
            tauxOcc = BigDecimal.valueOf(contratsActifs)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(biensTotal), 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        List<MoisMontantResponse> historique = buildHistoriqueRevenus(keycloakId, YearMonth.of(annee, mois));

        return new ProprietaireDashboardOverviewResponse(
                biensTotal,
                biensDispo,
                biensLoues,
                biensVendus,
                biensCompromis,
                patrimoine,
                contratsTotal,
                contratsActifs,
                contratsEnAttenteCandidatures,
                mandatsTotal,
                mandatsActifs,
                mandatsEnAttente,
                loyerTheorique,
                revenusMois,
                tauxOcc,
                annee,
                mois,
                historique
        );
    }

    private List<MoisMontantResponse> buildHistoriqueRevenus(String keycloakId, YearMonth periodeFin) {
        List<MoisMontantResponse> out = new ArrayList<>(6);
        YearMonth debut = periodeFin.minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth ym = debut.plusMonths(i);
            var d = ym.atDay(1).atStartOfDay();
            var f = ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000);
            BigDecimal m = versementRepository.sumMontantPourProprietaireEntre(keycloakId, d, f);
            if (m == null) {
                m = BigDecimal.ZERO;
            }
            out.add(new MoisMontantResponse(ym.getYear(), ym.getMonthValue(), m));
        }
        return out;
    }
}
