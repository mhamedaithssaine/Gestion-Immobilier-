package com.example.gestionimmobilier.service.dashboard;

import com.example.gestionimmobilier.dto.dashboard.*;
import com.example.gestionimmobilier.dto.finance.ResteAPayerResponse;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.ResourceNotFoundException;
import com.example.gestionimmobilier.models.entity.contrat.Bail;
import com.example.gestionimmobilier.models.entity.finance.Versement;
import com.example.gestionimmobilier.models.enums.StatutBail;
import com.example.gestionimmobilier.models.enums.StatutBien;
import com.example.gestionimmobilier.models.enums.StatutMandat;
import com.example.gestionimmobilier.models.entity.user.Agent;
import com.example.gestionimmobilier.repository.AgenceRepository;
import com.example.gestionimmobilier.repository.AgentRepository;
import com.example.gestionimmobilier.repository.BailRepository;
import com.example.gestionimmobilier.repository.BienImmobilierRepository;
import com.example.gestionimmobilier.repository.MandatDeGestionRepository;
import com.example.gestionimmobilier.repository.VersementRepository;
import com.example.gestionimmobilier.service.finance.VersementService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final BienImmobilierRepository bienRepository;
    private final VersementRepository versementRepository;
    private final BailRepository bailRepository;
    private final VersementService versementService;
    private final MandatDeGestionRepository mandatRepository;
    private final AgenceRepository agenceRepository;
    private final AgentRepository agentRepository;

    public DashboardService(BienImmobilierRepository bienRepository,
                            VersementRepository versementRepository,
                            BailRepository bailRepository,
                            VersementService versementService,
                            MandatDeGestionRepository mandatRepository,
                            AgenceRepository agenceRepository,
                            AgentRepository agentRepository) {
        this.bienRepository = bienRepository;
        this.versementRepository = versementRepository;
        this.bailRepository = bailRepository;
        this.versementService = versementService;
        this.mandatRepository = mandatRepository;
        this.agenceRepository = agenceRepository;
        this.agentRepository = agentRepository;
    }

    public NombreBiensDisponiblesResponse getNombreBiensDisponibles() {
        long count = bienRepository.countByStatut(StatutBien.DISPONIBLE);
        return new NombreBiensDisponiblesResponse(count);
    }

    public BiensLouesVsLibresResponse getBiensLouesVsLibres() {
        long disponibles = bienRepository.countByStatut(StatutBien.DISPONIBLE);
        long loues = bienRepository.countByStatut(StatutBien.LOUE);
        long vendus = bienRepository.countByStatut(StatutBien.VENDU);
        long sousCompromis = bienRepository.countByStatut(StatutBien.SOUS_COMPROMIS);
        return new BiensLouesVsLibresResponse(disponibles, loues, vendus, sousCompromis);
    }

    public RevenusMensuelsResponse getRevenusMensuels(int annee, int mois) {
        YearMonth ym = YearMonth.of(annee, mois);
        var debut = ym.atDay(1).atStartOfDay();
        var fin = ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000);
        List<Versement> versements = versementRepository.findByDateVersementBetween(debut, fin);
        BigDecimal total = versements.stream()
                .map(Versement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenusMensuelsResponse(annee, mois, total);
    }

    public LocatairesEnRetardResponse getLocatairesEnRetard(int annee, int mois) {
        List<Bail> bauxActifs = bailRepository.findByStatutOrderByDateDebutDesc(StatutBail.ACTIF);
        List<LocataireEnRetardLigneResponse> lignes = new ArrayList<>();
        for (Bail bail : bauxActifs) {
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
        return new LocatairesEnRetardResponse(lignes);
    }

    public MandatsGestionStatistiqueResponse getStatistiqueMandatsPourAgence(UUID agenceId) {
        var agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.AGENCE_INTROUVABLE));
        long actifs = mandatRepository.countByAgent_Agence_IdAndStatut(agenceId, StatutMandat.ACTIF);
        long resilies = mandatRepository.countByAgent_Agence_IdAndStatut(agenceId, StatutMandat.RESILIE);
        long termines = mandatRepository.countByAgent_Agence_IdAndStatut(agenceId, StatutMandat.TERMINE);
        long enAttente = mandatRepository.countByAgent_Agence_IdAndStatut(agenceId, StatutMandat.EN_ATTENTE);
        long total = mandatRepository.countByAgent_Agence_Id(agenceId);
        return new MandatsGestionStatistiqueResponse(
                agenceId,
                agence.getNom(),
                total,
                actifs,
                resilies,
                termines,
                enAttente
        );
    }

    public List<MandatsGestionStatistiqueResponse> getStatistiquesMandatsToutesAgences() {
        List<MandatsGestionStatistiqueResponse> result = new ArrayList<>();
        agenceRepository.findAllByOrderByCreatedAtDesc().forEach(agence ->
                result.add(getStatistiqueMandatsPourAgence(agence.getId()))
        );
        return result;
    }

    public MandatsGestionParAgentResponse getStatistiqueMandatsPourAgent(UUID agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.UTILISATEUR_INTROUVABLE));
        String nomAgent = formatNom(agent.getFirstName(), agent.getLastName());
        String agenceNom = agent.getAgence() != null ? agent.getAgence().getNom() : (agent.getAgenceNom() != null ? agent.getAgenceNom() : "");
        long actifs = mandatRepository.countByAgent_IdAndStatut(agentId, StatutMandat.ACTIF);
        long resilies = mandatRepository.countByAgent_IdAndStatut(agentId, StatutMandat.RESILIE);
        long termines = mandatRepository.countByAgent_IdAndStatut(agentId, StatutMandat.TERMINE);
        long enAttente = mandatRepository.countByAgent_IdAndStatut(agentId, StatutMandat.EN_ATTENTE);
        long total = mandatRepository.countByAgent_Id(agentId);
        return new MandatsGestionParAgentResponse(
                agentId,
                nomAgent,
                agenceNom,
                total,
                actifs,
                resilies,
                termines,
                enAttente
        );
    }

    private static String formatNom(String firstName, String lastName) {
        if (firstName == null && lastName == null) return "";
        if (firstName == null) return lastName != null ? lastName : "";
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
}
