package com.example.gestionimmobilier.service.dashboard;

import com.example.gestionimmobilier.dto.dashboard.AgentDashboardOverviewResponse;
import com.example.gestionimmobilier.dto.dashboard.LocataireEnRetardLigneResponse;
import com.example.gestionimmobilier.dto.finance.ResteAPayerResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.models.entity.agence.Agence;
import com.example.gestionimmobilier.models.entity.agence.AgenceModificationDemande;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.enums.StatutDemandeModificationAgence;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.models.enums.TypeDemandeModificationAgence;
import com.example.gestionimmobilier.repository.AgentRepository;
import com.example.gestionimmobilier.repository.AgenceModificationDemandeRepository;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.VersementRepository;
import com.example.gestionimmobilier.service.finance.VersementService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgentDashboardService {

    private final AgentRepository agentRepository;
    private final MandatDeGestionRepository mandatRepository;
    private final BailRepository bailRepository;
    private final VersementRepository versementRepository;
    private final VersementService versementService;
    private final AgenceModificationDemandeRepository agenceModificationDemandeRepository;

    public AgentDashboardService(
            AgentRepository agentRepository,
            MandatDeGestionRepository mandatRepository,
            BailRepository bailRepository,
            VersementRepository versementRepository,
            VersementService versementService,
            AgenceModificationDemandeRepository agenceModificationDemandeRepository) {
        this.agentRepository = agentRepository;
        this.mandatRepository = mandatRepository;
        this.bailRepository = bailRepository;
        this.versementRepository = versementRepository;
        this.versementService = versementService;
        this.agenceModificationDemandeRepository = agenceModificationDemandeRepository;
    }

    public AgentDashboardOverviewResponse getOverview(String keycloakId, int annee, int mois) {
        agentRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));

        long mandatsActifs = mandatRepository.countByAgentKeycloakIdAndStatut(keycloakId, StatutMandat.ACTIF);
        long mandatsTotal = mandatRepository.countByAgentKeycloakId(keycloakId);
        long biensDistincts = mandatRepository.countDistinctBiensPourAgentKeycloakId(keycloakId);
        StatutMandat mandatActif = StatutMandat.ACTIF;
        long bauxActifs = bailRepository.countByAgentKeycloakIdAndStatut(keycloakId, StatutBail.ACTIF, mandatActif);
        long demandesAgent = bailRepository.countByAgentKeycloakIdAndStatut(
                keycloakId, StatutBail.EN_ATTENTE_VALIDATION_AGENT, mandatActif);
        long locataires = bailRepository.countDistinctClientsPourAgentEtStatut(keycloakId, StatutBail.ACTIF, mandatActif);

        YearMonth ym = YearMonth.of(annee, mois);
        var debut = ym.atDay(1).atStartOfDay();
        var fin = ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000);
        BigDecimal revenus = versementRepository.sumMontantPourAgentEntre(keycloakId, debut, fin, mandatActif);
        if (revenus == null) {
            revenus = BigDecimal.ZERO;
        }

        List<LocataireEnRetardLigneResponse> retard = buildRetardPourAgent(keycloakId, annee, mois, mandatActif);

        long biensDispo = mandatRepository.countDistinctBiensPourAgentMandatEtStatutBien(
                keycloakId, mandatActif, StatutBien.DISPONIBLE);
        long biensLoues = mandatRepository.countDistinctBiensPourAgentMandatEtStatutBien(
                keycloakId, mandatActif, StatutBien.LOUE);
        long biensVendus = mandatRepository.countDistinctBiensPourAgentMandatEtStatutBien(
                keycloakId, mandatActif, StatutBien.VENDU);
        long biensCompromis = mandatRepository.countDistinctBiensPourAgentMandatEtStatutBien(
                keycloakId, mandatActif, StatutBien.SOUS_COMPROMIS);

        ModificationAgenceDash ctx = resolveModificationAgenceContext(keycloakId);

        return new AgentDashboardOverviewResponse(
                mandatsActifs,
                mandatsTotal,
                biensDistincts,
                bauxActifs,
                demandesAgent,
                locataires,
                revenus,
                annee,
                mois,
                retard,
                biensDispo,
                biensLoues,
                biensVendus,
                biensCompromis,
                ctx.demandeEnAttente,
                ctx.typeDemande,
                ctx.resumeDemande,
                ctx.noteAdmin,
                ctx.noteAdminLe
        );
    }

    private ModificationAgenceDash resolveModificationAgenceContext(String keycloakId) {
        Agent agent = agentRepository.findByKeycloakId(keycloakId).orElse(null);
        if (agent == null) {
            return ModificationAgenceDash.vide();
        }
        Agence agence = agent.getAgence();
        if (agence == null) {
            return ModificationAgenceDash.vide();
        }
        UUID aid = agence.getId();

        List<AgenceModificationDemande> pending = agenceModificationDemandeRepository.findByAgence_IdAndStatut(
                aid, StatutDemandeModificationAgence.EN_ATTENTE);
        boolean enAttente = !pending.isEmpty();
        String type = null;
        String resume = null;
        if (enAttente) {
            AgenceModificationDemande d = pending.get(pending.size() - 1);
            type = d.getType().name();
            resume = resumeDemande(d);
        }

        String note = null;
        LocalDateTime noteLe = null;
        List<AgenceModificationDemande> rejets = agenceModificationDemandeRepository.findDerniersRejetsAvecNote(
                aid, StatutDemandeModificationAgence.REJETE);
        for (AgenceModificationDemande d : rejets) {
            if (d.getCommentaireAdmin() != null && !d.getCommentaireAdmin().isBlank()) {
                note = d.getCommentaireAdmin().trim();
                noteLe = d.getResoluLe() != null ? d.getResoluLe() : d.getCreatedAt();
                break;
            }
        }

        return new ModificationAgenceDash(enAttente, type, resume, note, noteLe);
    }

    private static String resumeDemande(AgenceModificationDemande d) {
        if (d.getType() == TypeDemandeModificationAgence.SUPPRESSION) {
            return "Clôture de l’agence — validation administrateur requise.";
        }
        List<String> champs = new ArrayList<>();
        if (d.getNomPropose() != null && !d.getNomPropose().isBlank()) {
            champs.add("nom");
        }
        if (d.getEmailPropose() != null && !d.getEmailPropose().isBlank()) {
            champs.add("email");
        }
        if (d.getTelephonePropose() != null && !d.getTelephonePropose().isBlank()) {
            champs.add("téléphone");
        }
        if (d.getVillePropose() != null && !d.getVillePropose().isBlank()) {
            champs.add("ville");
        }
        if (d.getAdressePropose() != null && !d.getAdressePropose().isBlank()) {
            champs.add("adresse");
        }
        if (champs.isEmpty()) {
            return "Mise à jour de la fiche agence en attente de validation.";
        }
        return "Mise à jour proposée : " + String.join(", ", champs) + ".";
    }

    private record ModificationAgenceDash(
            boolean demandeEnAttente,
            String typeDemande,
            String resumeDemande,
            String noteAdmin,
            LocalDateTime noteAdminLe) {
        static ModificationAgenceDash vide() {
            return new ModificationAgenceDash(false, null, null, null, null);
        }
    }

    private List<LocataireEnRetardLigneResponse> buildRetardPourAgent(
            String keycloakId, int annee, int mois, StatutMandat mandatActif) {
        List<Bail> actifs = bailRepository.findByAgent_KeycloakIdAndStatutOrderByDateDebutDesc(
                keycloakId, StatutBail.ACTIF, mandatActif);
        List<LocataireEnRetardLigneResponse> lignes = new ArrayList<>();
        for (Bail bail : actifs) {
            ResteAPayerResponse reste = versementService.getResteAPayer(bail.getId(), annee, mois);
            if (reste.resteAPayer().compareTo(BigDecimal.ZERO) > 0) {
                String locataireNom = formatNom(bail.getClient().getFirstName(), bail.getClient().getLastName());
                String bienRef = bail.getBien() != null ? bail.getBien().getReference() : "";
                lignes.add(new LocataireEnRetardLigneResponse(
                        bail.getId(),
                        bail.getNumContrat() != null ? bail.getNumContrat() : "",
                        locataireNom,
                        bienRef,
                        annee,
                        mois,
                        reste.resteAPayer()
                ));
            }
        }
        return lignes;
    }

    private static String formatNom(String firstName, String lastName) {
        if (firstName == null && lastName == null) return "";
        if (firstName == null) return lastName != null ? lastName : "";
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
}
