# Planification — Gestion des contrats de location (BIEN-50 à BIEN-55)

Planning aligné sur les tâches Jira **IN PROGRESS** : Gestion des contrats de location + Historique des locations.

---

## Vue d’ensemble

| Tâche | Libellé | Statut actuel | Priorité |
|-------|---------|---------------|----------|
| **BIEN-51** | Créer un contrat | ✅ Fait (POST /api/admin/contrats) | — |
| **BIEN-55** | Associer contrat ↔ bien ↔ locataire | ✅ Fait (dans la création du bail) | — |
| **Prérequis** | Ajouter statut sur le bail | ❌ À faire | P0 |
| **BIEN-54** | Lister les contrats actifs | ❌ À faire | P1 |
| **BIEN-52** | Modifier un contrat | ❌ À faire | P2 |
| **BIEN-53** | Résilier un contrat | ❌ À faire | P2 |
| **BIEN-50** | Historique des locations | ❌ À faire | P3 |

---

## Phase 0 — Prérequis (avant BIEN-54 / 52 / 53 / 50)

**Objectif** : Pouvoir distinguer un contrat actif d’un contrat terminé ou résilié.

| # | Action | Détail | Livrable |
|---|--------|--------|----------|
| 0.1 | Créer l’enum `StatutBail` | `EN_ATTENTE`, `ACTIF`, `RESILIE`, `TERMINE` | `models/enums/StatutBail.java` |
| 0.2 | Ajouter le champ `statut` sur l’entité `Bail` | Colonne en base, valeur par défaut `ACTIF` à la création | Migration / entité `Bail` |
| 0.3 | Mettre à jour la création de contrat | Dans `ContratService.creerContrat`, setter `bail.setStatut(StatutBail.ACTIF)` (ou EN_ATTENTE) | `ContratService` + DTO response si besoin |
| 0.4 | Exposer le statut dans la réponse contrat | Ajouter `statut` dans `ContratResponse` | `ContratResponse` |

**Durée estimée** : 0,5 à 1 jour.

---

## Phase 1 — BIEN-54 : Lister les contrats actifs

**Objectif** : Endpoint pour récupérer la liste des baux avec filtre par statut (ex. actifs uniquement).

| # | Action | Détail | Livrable |
|---|--------|--------|----------|
| 1.1 | Méthode en repository | `BailRepository`: `List<Bail> findByStatut(StatutBail statut)` ou `findAllByStatutOrderByDateDebutDesc` | `BailRepository` |
| 1.2 | Méthode en service | `ContratService.listerContrats(StatutBail statut)` ou `listerContratsActifs()` | `ContratService` |
| 1.3 | Endpoint GET | `GET /api/admin/contrats?statut=ACTIF` (ou sans param = tous) | `ContratController` |
| 1.4 | Réponse liste | Retourner `List<ContratResponse>` (déjà le format d’un contrat) | Controller + ApiRetour |

**Critère de validation** : GET avec `statut=ACTIF` retourne uniquement les baux dont le statut est ACTIF.

**Durée estimée** : 0,5 à 1 jour.

---

## Phase 2 — BIEN-52 : Modifier un contrat

**Objectif** : Mettre à jour les données d’un bail existant (dates, loyer, charges, etc.).

| # | Action | Détail | Livrable |
|---|--------|--------|----------|
| 2.1 | DTO mise à jour | `UpdateContratRequest` (dates, loyerHC, charges, documentUrl, etc. — champs optionnels) | `dto/contrat/UpdateContratRequest.java` |
| 2.2 | Méthode service | `ContratService.modifierContrat(UUID id, UpdateContratRequest request)` : charger le bail, appliquer les champs, sauvegarder | `ContratService` |
| 2.3 | Règles métier | Ne pas modifier si statut = RESILIE ou TERMINE (ou autoriser selon specs) ; validation des dates (dateFin > dateDebut) | `ContratService` |
| 2.4 | Endpoint | `PUT` ou `PATCH /api/admin/contrats/{id}` avec body `UpdateContratRequest` | `ContratController` |

**Critère de validation** : PATCH avec un id de bail existant met à jour le bail et retourne le `ContratResponse` à jour.

**Durée estimée** : 1 jour.

---

## Phase 3 — BIEN-53 : Résilier un contrat

**Objectif** : Passer un bail en statut RÉSILIÉ (fin avant échéance).

| # | Action | Détail | Livrable |
|---|--------|--------|----------|
| 3.1 | Option : DTO résiliation | `ResilierContratRequest` (optionnel : dateRésiliation, motif) ou simple PATCH sans body | `dto/contrat/` si besoin |
| 3.2 | Méthode service | `ContratService.resilierContrat(UUID id)` : charger le bail, vérifier statut ACTIF, setter `statut = RESILIE`, sauvegarder | `ContratService` |
| 3.3 | Mise à jour statut bien | Si le bien était LOUE, repasser en DISPONIBLE (optionnel selon règles métier) | `ContratService` ou `BienService` |
| 3.4 | Endpoint | `PATCH /api/admin/contrats/{id}/resilier` ou `POST /api/admin/contrats/{id}/resilier` | `ContratController` |

**Critère de validation** : Après résiliation, le bail a le statut RÉSILIÉ et n’apparaît plus dans « contrats actifs ».

**Durée estimée** : 0,5 à 1 jour.

---

## Phase 4 — BIEN-50 : Historique des locations

**Objectif** : Consulter l’historique des baux (terminés / résiliés) par locataire ou global.

| # | Action | Détail | Livrable |
|---|--------|--------|----------|
| 4.1 | Lister tous les contrats avec filtre statut | Réutiliser ou étendre le GET de BIEN-54 : `?statut=TERMINE` ou `?statut=RESILIE` ou sans filtre | Déjà partiel (Phase 1) |
| 4.2 | Historique par locataire | `GET /api/admin/locataires/{id}/contrats` ou `GET /api/admin/contrats?locataireId=xxx` retournant la liste des baux (tous statuts ou seulement passés) | `ContratController` + `ContratService` |
| 4.3 | Repository | `BailRepository.findByClientIdOrderByDateDebutDesc(UUID clientId)` | `BailRepository` |
| 4.4 | Option : historique par bien | `GET /api/admin/biens/{id}/contrats` pour l’historique des locations d’un bien | Optionnel |

**Critère de validation** : Un appel par locataire (ou avec filtre) retourne la liste des baux passés et actuels.

**Durée estimée** : 1 jour.

---

## Ordre d’exécution recommandé (sprint / semaines)

```
Semaine 1 (ou Sprint 1)
├── Phase 0 — Prérequis (statut sur Bail)
├── Phase 1 — BIEN-54 Lister les contrats actifs
└── Phase 3 — BIEN-53 Résilier un contrat

Semaine 2 (ou Sprint 2)
├── Phase 2 — BIEN-52 Modifier un contrat
└── Phase 4 — BIEN-50 Historique des locations
```

Ordre logique si tu enchaînes d’un coup :

1. **Phase 0** (prérequis)  
2. **Phase 1** (BIEN-54) — liste actifs  
3. **Phase 3** (BIEN-53) — résilier (utilise le statut)  
4. **Phase 2** (BIEN-52) — modifier  
5. **Phase 4** (BIEN-50) — historique  

---

## Récap par tâche Jira (alignement planning)

| Jira | Libellé | Phase | Livrables principaux |
|------|---------|--------|------------------------|
| **BIEN-51** | Créer un contrat | Déjà fait | POST /api/admin/contrats, ContratService.creerContrat |
| **BIEN-55** | Associer contrat ↔ bien ↔ locataire | Déjà fait | Bail : bien, propriétaire, client (locataire) dans création |
| **BIEN-54** | Lister les contrats actifs | Phase 1 | GET /api/admin/contrats?statut=ACTIF, repository + service |
| **BIEN-52** | Modifier un contrat | Phase 2 | PATCH /api/admin/contrats/{id}, UpdateContratRequest, service |
| **BIEN-53** | Résilier un contrat | Phase 3 | PATCH /api/admin/contrats/{id}/resilier, service |
| **BIEN-50** | Historique des locations | Phase 4 | GET par locataire (ou filtre), repository findByClientId |

---

## Checklist globale (à cocher)

- [ ] **Phase 0** — Enum StatutBail + champ statut sur Bail + création contrat set statut + ContratResponse.statut
- [ ] **Phase 1** — BailRepository findByStatut + ContratService.listerContrats + GET /contrats?statut=
- [ ] **Phase 2** — UpdateContratRequest + ContratService.modifierContrat + PATCH /contrats/{id}
- [ ] **Phase 3** — ContratService.resilierContrat + PATCH /contrats/{id}/resilier
- [ ] **Phase 4** — BailRepository.findByClientId + GET locataires/{id}/contrats ou GET contrats?locataireId=

Tu peux copier ce plan dans Jira (résumé par ticket BIEN-50 à BIEN-55) ou l’utiliser comme référence pour tes sprints.
