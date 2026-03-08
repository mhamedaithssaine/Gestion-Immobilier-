# Workflow plateforme gestion immobilière (type Foncia)

## 1. Acteurs et relations

| Acteur | Rôle |
|--------|------|
| **Agent immobilier (agence)** | Représente l’agence ; gère les biens via mandat, publie les annonces, crée les baux, encaisse loyers et commissions. |
| **Propriétaire** | Détient les biens ; signe un mandat avec l’agence pour qu’elle gère le bien ; reçoit le produit des loyers (moins commission). |
| **Locataire (Client)** | Loue un bien ; signe un bail ; paie le loyer (et charges). |

**Relations :**
- **Propriétaire ↔ Bien** : 1-N (un propriétaire a plusieurs biens).
- **Propriétaire ↔ Agence** : via **mandat de gestion** (1 mandat = 1 bien confié à 1 agence/agent).
- **Propriétaire ↔ Locataire** : via **bail de location** (1 bail = 1 bien loué à 1 locataire).
- **Agent** : intervient sur le mandat (gestion du bien) et sur le bail (création, suivi).

---

## 2. Les deux types de contrats

### 2.1 Mandat de gestion (Propriétaire ↔ Agence)

- **Rôle** : Le propriétaire autorise l’agence (via un agent) à gérer son bien (mise en location, encaissement, entretien, etc.).
- **Création** : Le propriétaire (ou l’admin) assigne un **bien** à un **agent** ; le système enregistre un mandat (date début, durée/date fin, commission, statut).
- **Contenu typique** :  
  `num_mandat`, `proprietaire_id`, `agent_id` (ou `agence_id`), `bien_id`, `date_debut`, `date_fin` (ou `duree_mois`), `commission_pct` ou `commission_fixe`, `statut`, `date_signature`, `document_url`.
- **Effet** : Tant que le mandat est actif, l’agence peut publier le bien, créer des baux pour ce bien, encaisser les loyers et prélever sa commission.

### 2.2 Bail de location (Propriétaire ↔ Locataire)

- **Rôle** : Contrat de location du bien au locataire (loyer, durée, garantie, etc.).
- **Création** : Un agent (ou l’admin) crée le bail après réservation/candidature : choix du **bien**, du **propriétaire**, du **locataire**, optionnellement de l’**agent** en charge.
- **Contenu typique** :  
  `num_contrat`, `bien_id`, `proprietaire_id`, `client_id` (locataire), `agent_id`, `date_debut`, `date_fin`, `loyer_hors_charges`, `charges`, `depot_garantie`, `statut`, `date_signature`, `document_url`.
- **Effet** : Le locataire paie le loyer ; l’agence prélève sa commission (définie dans le mandat) et reverse le reste au propriétaire.

---

## 3. Workflow complet (étapes 1 à 8)

1. **Propriétaire crée un compte** → Utilisateur (Propriétaire).
2. **Il ajoute un bien immobilier** → Entité Bien (lié au propriétaire).
3. **Il signe un mandat de gestion avec une agence** → Mandat (propriétaire + agent/agence + bien) ; statut ACTIF.
4. **L’agence publie le bien à louer** → Le bien passe en DISPONIBLE (ou équivalent) ; annonce/candidatures côté métier.
5. **Un locataire réserve / postule** → Candidature ou réservation (hors scope des tâches actuelles du tableau).
6. **L’agence crée le bail de location** → Bail (bien + propriétaire + locataire + optionnellement agent) ; statut ACTIF.
7. **Le locataire paie le loyer mensuellement** → Versements (paiements) liés au bail.
8. **L’agence prélève sa commission et transfère le reste au propriétaire** → Règles métier / finance (calcul commission depuis mandat, versement au propriétaire).

---

## 4. Schéma des relations entre entités

```
                    +------------------+
                    |   Proprietaire   |
                    +--------+---------+
                             |
         +-------------------+-------------------+
         |                   |                   |
         v                   v                   v
+----------------+  +----------------+  +------------------+
| BienImmobilier |  | MandatDeGestion|  |       Bail        |
+--------+-------+  +-------+--------+  +---------+---------+
         |                |                      |
         |                | agent_id              | client_id
         |                v                       v
         |         +-------------+         +------------+
         +-------->|    Agent     |<--------+  Client    |
                  +-------------+          (Locataire)  |
                                                       |
                  +----------------+                   |
                  |   Versement    |<------------------+
                  | (bail_id, ...) |
                  +----------------+
```

- **MandatDeGestion** : `proprietaire_id`, `agent_id`, `bien_id`, dates, commission, statut.
- **Bail** (existant) : `proprietaire_id`, `client_id`, `agent_id`, `bien_id`, loyer, charges, dates, statut (à ajouter).
- **Versement** (existant) : lié au bail (et éventuellement au mandat pour tracer la commission).

---

## 5. Modélisation base de données (tables principales)

### Déjà présentes dans le projet

| Table | Rôle |
|-------|------|
| `utilisateurs` (héritage: agents, proprietaires, clients, admins) | Acteurs (agent, propriétaire, locataire). |
| `biens_immobiliers` (+ apparts, maisons) | Biens avec propriétaire_id. |
| `contrats` (abstraite) / `baux` | Bail de location (bien, propriétaire, client, agent, loyer, charges, dates). |
| `versements` | Paiements liés à un bail et un propriétaire. |

### À ajouter pour le workflow complet

| Table | Rôle |
|-------|------|
| **mandats_gestion** | Mandat de gestion : `proprietaire_id`, `agent_id`, `bien_id`, `num_mandat`, `date_debut`, `date_fin`, `commission_pct` ou `commission_fixe`, `statut`, `date_signature`, `document_url`. |
| **Statut bail** | Colonne `statut` dans `baux` (ex. ACTIF, RESILIE, TERMINE, EN_ATTENTE) pour BIEN-54 / BIEN-53. |
| **Statut mandat** | Enum/colonne `statut` pour mandats (ACTIF, RESILIE, TERMINE). |

### Relations entre tables (résumé)

- **owner (proprietaire)** → biens_immobiliers (1-N), mandats_gestion (1-N), baux (1-N), versements (1-N).
- **agent** → mandats_gestion (1-N), baux (0-N, optionnel).
- **property (bien)** → 1 mandat actif possible à la fois (contrainte métier), 1 bail actif à la fois ; baux (1-N historique).
- **mandate_contract (mandats_gestion)** → proprietaire, agent, bien.
- **lease_contract (baux)** → bien, proprietaire, client (locataire), agent (optionnel).
- **payments (versements)** → bail, proprietaire (déjà en place).

---

## 6. Statuts des contrats

### Bail (recommandé)

| Statut | Signification |
|--------|----------------|
| `EN_ATTENTE` | Créé, pas encore effectif (ex. signature en cours). |
| `ACTIF` | En cours ; le locataire doit payer le loyer. |
| `RESILIE` | Résilié avant la date fin (BIEN-53). |
| `TERMINE` | Arrivé à échéance normalement (pour historique BIEN-50). |

### Mandat de gestion

| Statut | Signification |
|--------|----------------|
| `EN_ATTENTE` | Signé mais pas encore effectif. |
| `ACTIF` | L’agence gère le bien. |
| `RESILIE` | Résilié par l’une des parties. |
| `TERMINE` | Arrivé à échéance. |

---

## 7. Correspondance avec les tâches du tableau (Jira)

| Tâche | Description | Côté projet actuel | À faire |
|-------|-------------|--------------------|--------|
| **BIEN-51** | Créer un contrat | Déjà : création d’un **bail** (POST contrat). | OK côté bail. Pour mandat : à développer (voir ci‑dessous). |
| **BIEN-55** | Associer contrat ↔ bien ↔ locataire | Le bail associe déjà bien + propriétaire + locataire (client). | Rien à changer en DB pour le bail. |
| **BIEN-54** | Lister les contrats actifs | Pas de filtre par statut aujourd’hui (pas de champ `statut` sur le bail). | **DB** : ajouter `statut` sur `baux`. **Backend** : endpoint GET contrats?statut=ACTIF. |
| **BIEN-52** | Modifier un contrat | Non implémenté. | **Backend** : PUT/PATCH contrat (bail) avec règles métier (dates, loyer, etc.). |
| **BIEN-53** | Résilier un contrat | Non implémenté. | **DB** : `statut` sur baux. **Backend** : PATCH contrat → statut RESILIE (+ date résiliation si besoin). |
| **BIEN-50** | Historique des locations | Pas d’endpoint dédié. | **Backend** : GET contrats (avec statut TERMINE/RESILIE) ou GET locataires/{id}/contrats (historique par locataire). |

---

## 8. Ce qu’il faut changer (résumé)

### Base de données / code

1. **Ajouter l’entité Mandat de gestion**
   - Table `mandats_gestion` (ou `mandate_contract`) avec : proprietaire_id, agent_id, bien_id, num_mandat, date_debut, date_fin, commission (%), statut, date_signature, document_url.
   - Enum `StatutMandat` (ex. ACTIF, RESILIE, TERMINE, EN_ATTENTE).
   - Pas d’héritance avec `Contrat` (relation distincte : pas de locataire).

2. **Ajouter le statut sur le bail**
   - Colonne `statut` dans `baux` (enum `StatutBail` : ACTIF, RESILIE, TERMINE, EN_ATTENTE).
   - Création de bail : statut par défaut ACTIF (ou EN_ATTENTE si vous gérez une phase de signature).

3. **Optionnel**
   - Lien explicite bien ↔ mandat actif (pour vérifier qu’un bien est bien sous mandat avant de créer un bail).
   - Dépôt de garantie sur le bail si besoin (champ `depot_garantie`).

### Jira (backlog)

1. **Garder** les tâches actuelles (BIEN-50 à BIEN-55) pour le **bail de location** ; les compléter avec les implémentations ci‑dessus (statut, liste actifs, modifier, résilier, historique).

2. **Ajouter** un bloc “Gestion des mandats de gestion” (par exemple une epic ou un label), avec des tâches du même type que pour les baux :
   - Créer un mandat de gestion (propriétaire + bien + agent, dates, commission, statut).
   - Associer mandat ↔ bien ↔ agent (propriétaire implicite).
   - Lister les mandats actifs.
   - Modifier un mandat.
   - Résilier un mandat.
   - Historique des mandats.

3. **Clarifier** dans les descriptions :
   - BIEN-51 / BIEN-55 : contrat = **bail de location** (propriétaire–locataire).
   - Les nouvelles tâches : contrat = **mandat de gestion** (propriétaire–agence).

Ainsi, le workflow (étapes 1 à 8) est couvert, les tâches du tableau sont alignées avec la base de données et le backlog Jira reflète les deux types de contrats.
