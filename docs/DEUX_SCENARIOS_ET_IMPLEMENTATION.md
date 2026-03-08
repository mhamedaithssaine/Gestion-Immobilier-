# Plateforme gestion immobilière (type Foncia) — Deux scénarios et implémentation

## 1. Les deux scénarios en résumé

| | **Scénario 1 — Bien géré par une agence** | **Scénario 2 — Bien géré par le propriétaire** |
|---|-------------------------------------------|-----------------------------------------------|
| Qui gère le bien ? | L’agence (via un agent) | Le propriétaire lui‑même |
| Contrat propriétaire ↔ agence | **Oui** : mandat de gestion | **Non** |
| Qui publie le bien ? | L’agence | Le propriétaire |
| Qui signe le bail avec le locataire ? | L’agence (au nom du propriétaire) | Le propriétaire directement |
| Qui encaisse le loyer ? | L’agence (puis reverse au propriétaire après commission) | Le propriétaire directement |
| Champ `agent_id` sur le bail | Renseigné (agent en charge) | **Null** (gestion directe) |

---

## 2. Scénario 1 — Bien géré par une agence

### Workflow

1. Le **propriétaire** crée un compte sur la plateforme.
2. Il **ajoute un bien immobilier** (appartement, maison).
3. Le propriétaire **signe un contrat de gestion (mandat)** avec une agence immobilière.
4. Une fois le mandat **actif**, l’agence devient responsable de la gestion du bien.
5. L’**agence publie** le bien à louer sur la plateforme.
6. Quand un locataire est trouvé, l’**agence crée et signe le bail** avec le locataire **au nom du propriétaire**.
7. Le **locataire paie le loyer** (à l’agence ou via la plateforme).
8. L’**agence collecte** le loyer, **prélève sa commission** (définie dans le mandat) et **transfère le reste au propriétaire**.

### Le mandat de gestion (propriétaire ↔ agence)

- **Rôle** : Contrat par lequel le propriétaire autorise l’agence à gérer son bien (publication, recherche de locataire, signature du bail, encaissement des loyers, entretien, etc.).
- **Parties** : Propriétaire (donneur d’ordre) / Agence (mandataire, représentée par un ou plusieurs agents).
- **Contenu typique** : numéro de mandat, bien concerné, agent(s) en charge, date début / date fin (ou durée), type de mandat (exclusif / non exclusif), **commission** (%), éventuellement dépôt de garantie géré par l’agence, **statut** (EN_ATTENTE, ACTIF, RÉSILIÉ, TERMINÉ).
- **Effet** : Tant que le mandat est actif, seul l’agence (ou le propriétaire selon les clauses) peut publier le bien et signer des baux pour ce bien. Les paiements du locataire peuvent transiter par l’agence.

### Le contrat de bail (locataire ↔ agence au nom du propriétaire)

- **Parties au bail** : Locataire (Client) d’un côté ; **propriétaire** de l’autre (le bail engage le propriétaire).
- **Rôle de l’agence** : L’agence **agit au nom du propriétaire** : elle crée le bail, le signe, peut encaisser les loyers, prélever sa commission et reverser le reste au propriétaire. En base, le bail reste lié au **propriétaire** et au **bien** ; l’**agent** (optionnel) indique qui gère ce bail pour le compte de l’agence.
- **Contenu typique du bail** : numéro de contrat, bien, propriétaire, locataire (client), **agent** (si gestion agence), date début / date fin, loyer HC, charges, dépôt de garantie, **statut** (ACTIF, RÉSILIÉ, TERMINÉ, etc.).

### Rôle de l’agence dans la gestion du bien

- Publication du bien (changement de statut bien, annonces).
- Mise en relation avec les locataires et création des baux.
- Encaissement des loyers (ou suivi des paiements sur la plateforme).
- Prélèvement de la commission (selon le mandat).
- Transfert du solde au propriétaire.
- Gestion courante (entretien, interventions) si prévu au mandat.

---

## 3. Scénario 2 — Bien géré directement par le propriétaire

### Workflow

1. Le **propriétaire** crée un compte.
2. Il **ajoute son bien** immobilier.
3. Il **choisit de ne pas confier** la gestion à une agence (pas de mandat).
4. Le **propriétaire publie lui‑même** son bien à louer.
5. Quand un locataire est trouvé, le **bail est signé directement** entre le propriétaire et le locataire (pas d’agent).
6. Le **locataire paie le loyer directement au propriétaire** (pas de passage par l’agence).

### Différence principale avec le scénario 1

- **Pas de mandat** : aucune table `management_contracts` (mandats) pour ce bien.
- **Pas d’agent sur le bail** : dans la table des baux, `agent_id` est **NULL** pour un bail en gestion directe.
- **Paiements** : les versements restent liés au bail et au propriétaire ; ils sont effectués directement par le locataire vers le propriétaire (pas de commission agence à déduire).

En résumé : **même modèle de bail** (propriétaire + bien + locataire), avec ou sans `agent_id`. La présence d’un **mandat actif** pour le bien indique le scénario « agence » ; l’absence de mandat (ou mandat résilié) + `agent_id = null` sur le bail indique le scénario « gestion directe ».

---

## 4. Schéma de la base de données

### Tables principales (alignées avec ton projet + ajouts)

| Table (logique) | Table physique actuelle / à créer | Rôle |
|----------------|-----------------------------------|------|
| **users** | `utilisateurs` (+ `admins`, `agents`, `clients`, `proprietaires`) | Tous les comptes (admin, agent, locataire, propriétaire). |
| **agencies** | `agences` (optionnel) ou dérivé de `agents.agence_nom` | Une agence regroupe des agents. Aujourd’hui : pas de table, uniquement `agents.agence_nom`. |
| **owners** | `proprietaires` (héritage `utilisateurs`) | Propriétaires. |
| **properties** | `biens_immobiliers` (+ `appartements`, `maisons`, `adresses`, `bien_images`) | Biens avec `proprietaire_id`, `statut`. |
| **management_contracts** | `mandats_gestion` (à créer) | Mandat propriétaire ↔ agence : `proprietaire_id`, `agent_id`, `bien_id`, dates, commission, statut. |
| **lease_contracts** | `contrats` + `baux` | Bail : `proprietaire_id`, `client_id`, `bien_id`, `agent_id` (nullable), loyer, charges, dates, **statut** (à ajouter). |
| **payments** | `versements` | Paiements : `bail_id`, `proprietaire_id`, montant, mode, référence, etc. |

### Relations entre entités

```
utilisateurs (users)
├── admins
├── agents          ──► optionnel: agence_id (si table agences)
├── clients         (locataires)
└── proprietaires   (owners)

proprietaires ──► biens_immobiliers (1-N)
                 ──► mandats_gestion (1-N)   [Scénario 1]
                 ──► baux (1-N)              [bail: propriétaire du bien]

agents ──► mandats_gestion (1-N)   [agent en charge du mandat]
         ──► baux (0-N)            [agent_id nullable, rempli si gestion agence]

biens_immobiliers ──► mandats_gestion (0 ou 1 actif)  [un bien sous mandat ou pas]
                    ──► baux (0-N, historique)        [bail actif ou passé]

clients (locataires) ──► baux (1-N)   [historique des locations]

baux ──► versements (1-N)
```

### Statuts recommandés

**Biens (`statut_bien` — déjà présent : DISPONIBLE, LOUE, VENDU, SOUS_COMPROMIS)**  
- DISPONIBLE : à louer (gestion directe ou agence).  
- LOUE : un bail actif existe.  
- VENDU / SOUS_COMPROMIS : hors location si besoin.

**Mandats de gestion (`statut_mandat` — à créer)**  
- EN_ATTENTE : signé, pas encore effectif.  
- ACTIF : l’agence gère le bien.  
- RÉSILIÉ : terminé avant échéance.  
- TERMINÉ : arrivé à échéance.

**Baux (`statut_bail` — à ajouter sur la table `baux`)**  
- EN_ATTENTE : en cours de signature.  
- ACTIF : bail en cours, loyer dû.  
- RÉSILIÉ : résilié avant la date fin.  
- TERMINÉ : arrivé à échéance (pour historique).

---

## 5. Ce qui existe déjà vs ce qu’il faut faire

### Déjà en place (à réutiliser tel quel ou avec petits ajouts)

| Élément | État | Remarque |
|--------|------|-----------|
| **Users / owners** | OK | `utilisateurs` → `proprietaires`, `clients`, `agents`, `admins`. |
| **Properties** | OK | `biens_immobiliers`, `proprietaire_id`, `statut` (StatutBien). |
| **Lease contracts (baux)** | OK en structure | `contrats` + `baux` avec `proprietaire_id`, `client_id`, `bien_id`, `agent_id` **nullable**. Gestion directe = créer un bail avec `agent_id = null`. |
| **Payments** | OK | `versements` (bail_id, proprietaire_id, montant, etc.). |
| **Création d’un bail** | OK | ContratService.creerContrat (POST /api/admin/contrats). |

Donc le **scénario 2 (gestion directe)** est déjà supporté par le modèle : il suffit de créer un bail **sans** renseigner d’agent (ou avec `agent_id = null`).

### Changements à prévoir

#### 1) Base de données / entités

- **Ajouter la table `mandats_gestion` (management_contracts)**  
  - Colonnes utiles : `id`, `num_mandat` (unique), `proprietaire_id`, `agent_id`, `bien_id`, `date_debut`, `date_fin`, `commission_pct` (ou montant), `statut` (enum StatutMandat), `date_signature`, `document_url`, timestamps.  
  - Contrainte métier : pour un même bien, un seul mandat actif à la fois (optionnel en base par trigger ou en applicatif).

- **Ajouter un statut sur le bail**  
  - Colonne `statut` dans `baux` (enum `StatutBail` : EN_ATTENTE, ACTIF, RÉSILIÉ, TERMINÉ).  
  - À la création d’un bail : mettre par défaut ACTIF (ou EN_ATTENTE si vous gérez une phase de signature).  
  - Permet de lister les « contrats actifs », résilier (passage en RÉSILIÉ), et garder l’historique (TERMINÉ).

- **Table `agences` (optionnel)**  
  - Aujourd’hui : `agents.agence_nom` (chaîne). Si vous voulez une vraie entité Agence (adresse, SIRET, plusieurs agents) : créer table `agences`, puis `agent.agence_id` (FK) à la place de ou en plus de `agence_nom`. Sinon, on peut rester avec `agence_nom` pour une V1.

#### 2) Règles métier / API

- **Scénario 1**  
  - À la création d’un bail pour un bien **sous mandat** : vérifier qu’un mandat actif existe pour ce bien (et éventuellement que l’agent qui crée le bail est bien celui du mandat).  
  - Renseigner `agent_id` sur le bail avec l’agent en charge.  
  - Commission : à calculer côté métier / finance à partir du mandat (commission_pct) et des versements ; pas obligatoire en base pour la première version.

- **Scénario 2**  
  - Création d’un bail **sans** mandat pour ce bien : `agent_id = null`.  
  - Pas de commission à reverser à l’agence (ou règle métier « 0 % »).

- **Publication du bien**  
  - Uniquement mettre à jour le statut du bien (ex. DISPONIBLE) et éventuellement une date de publication. Pas de table obligatoire supplémentaire si vous n’avez pas encore « annonces » séparées.

- **Optionnel**  
  - Endpoints : GET mandats (actifs, par bien, par propriétaire), POST mandat, PATCH résiliation mandat.  
  - GET baux?statut=ACTIF, PATCH bail (modifier / résilier).

---

## 6. Plan d’implémentation concret

### Phase 1 — Sans casser l’existant

1. **Créer l’entité `MandatDeGestion`** (table `mandats_gestion`) + enum `StatutMandat`.  
2. **Ajouter `statut` au bail** : enum `StatutBail` + colonne en base sur `baux` ; à la création d’un bail, définir `statut = ACTIF`.  
3. **Adapter la création de bail** (optionnel mais recommandé) :  
   - Si le bien a un mandat actif → remplir `agent_id` avec l’agent du mandat (ou l’agent choisi si plusieurs).  
   - Si le bien n’a pas de mandat actif → laisser `agent_id = null` (gestion directe).

Aucun changement obligatoire sur les tables existantes (utilisateurs, biens, versements), sauf l’ajout de la colonne `statut` sur `baux`.

### Phase 2 — Règles et API

4. **CRUD mandat** : création, lecture, liste (par propriétaire, par bien, actifs), résiliation (changement de statut).  
5. **Filtrage des baux** : GET contrats?statut=ACTIF (pour « lister les contrats actifs »).  
6. **Résiliation / modification de bail** : PATCH pour passer un bail en RÉSILIÉ ou TERMINÉ, et éventuellement modifier dates ou montants selon vos règles.

### Phase 3 (optionnel)

7. **Table `agences`** + lien `agents.agence_id` si vous voulez gérer plusieurs agences proprement.  
8. **Module paiements** : calcul de la commission à partir du mandat, enregistrement du reversement au propriétaire (peut rester en dur dans un premier temps).

---

## 7. Résumé : deux scénarios, un seul modèle de bail

- **Un seul type de contrat de location** : le **bail** (table `baux`), toujours entre **propriétaire** et **locataire** (client).  
- **Différence scénario 1 / 2** :  
  - Scénario 1 : il existe un **mandat actif** pour le bien + le bail a un **agent_id** renseigné (l’agence agit au nom du propriétaire).  
  - Scénario 2 : **pas de mandat** (ou plus actif) pour le bien + le bail a **agent_id = null** (gestion directe).  
- **Changements à faire** : ajouter **mandats_gestion** + **statut sur les baux** ; le reste (users, properties, baux, payments) reste compatible avec les deux scénarios. Aucun besoin de dupliquer les tables de baux ou de propriétés.

Une fois ces ajouts en place, tu pourras implémenter les tâches Jira (création contrat, association contrat–bien–locataire, lister actifs, modifier, résilier, historique) pour les deux cas (avec ou sans agence) en t’appuyant sur le même modèle de données.

---

## 8. État d’implémentation (alignement avec les deux scénarios)

| Élément | Scénario 1 (agence) | Scénario 2 (gestion directe) |
|--------|----------------------|------------------------------|
| **Mandat de gestion** | ✅ Entité `MandatDeGestion`, table `mandats_gestion`, enum `StatutMandat`. CRUD : POST/GET/PATCH résilier `/api/admin/mandats`. | — (pas de mandat) |
| **Création bail avec agent** | ✅ POST `/api/admin/contrats` avec `agentId` renseigné. Si le bien a un mandat actif et que `agentId` est omis, l’agent du mandat est utilisé. | — |
| **Création bail sans agent** | — | ✅ POST `/api/admin/contrats` avec `agentId` = null (ou omis). |
| **Bail (agent_id nullable)** | ✅ Renseigné (agent en charge). | ✅ Null. |
| **Statut bail** | ✅ ACTIF, RÉSILIÉ, TERMINÉ, EN_ATTENTE. | ✅ Idem. |

Les deux scénarios sont respectés : scénario 1 (mandat + bail avec agent), scénario 2 (pas de mandat, bail sans agent).
