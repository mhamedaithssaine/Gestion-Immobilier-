# SKNA — Plateforme de gestion immobilière

Application web **full stack** pour la gestion d’agences immobilières, des biens (appartements, maisons), des mandats de gestion, des baux, des versements et des parcours **propriétaire**, **locataire**, **agent** et **administrateur**.  
Le front-office public permet la consultation des annonces et l’inscription ; les espaces connectés s’appuient sur **OAuth2 / JWT (Keycloak)**.

---

## Sommaire

1. [Vue d’ensemble](#vue-densemble)
2. [Architecture du dépôt](#architecture-du-dépôt)
3. [Stack technique](#stack-technique)
4. [Prérequis](#prérequis)
5. [Démarrage rapide](#démarrage-rapide)
6. [Configuration](#configuration)
7. [Rôles et espaces applicatifs](#rôles-et-espaces-applicatifs)
8. [API REST (aperçu)](#api-rest-aperçu)
9. [Qualité, tests et CI](#qualité-tests-et-ci)
10. [Documentation complémentaire](#documentation-complémentaire)

---

## Vue d’ensemble

| Domaine | Description |
|--------|-------------|
| **Biens** | Création, publication, statuts (disponible, loué, vendu, etc.), médias (stockage local ou cloud). |
| **Mandats** | Lien propriétaire — agent — bien, suivi des statuts. |
| **Contrats / baux** | Demandes de location, validation agent ou propriétaire selon le mandat, résiliation, suivi des statuts. |
| **Finance** | Versements, reste à payer, indicateurs pour tableaux de bord. |
| **Utilisateurs** | Inscription publique (locataire / propriétaire), activation par admin, gestion via Keycloak. |
| **Agences** | Enregistrement d’agence, validation admin, espace agent (tableau de bord, biens, mandats, baux). |

---

## Architecture du dépôt

```
gesiotion-immobilier/
├── backend/                 # API Spring Boot (Java 17)
│   ├── src/main/java/       # Code applicatif (controllers, services, domaine)
│   ├── src/main/resources/  # application.properties, logback, etc.
│   ├── docker/              # Keycloak, CI, monitoring (compose)
│   └── pom.xml
├── frontend/                # SPA Angular (standalone components, lazy routes)
│   └── src/app/
│       ├── core/            # Guards, HTTP, auth, layout public
│       └── features/        # admin, agence, auth, home, locataire, propriétaire
└── README.md
```

---

## Stack technique

| Couche | Technologies |
|--------|----------------|
| **Backend** | Spring Boot 3.5.x, Spring Security (OAuth2 Resource Server), Spring Data JPA, Hibernate, PostgreSQL |
| **Frontend** | Angular 21, RxJS, formulaires template-driven, lazy loading |
| **Auth** | Keycloak (realm dédié, JWT) |
| **Transverse** | MapStruct, Lombok, validation Bean Validation, logs (Logback / option ELK) |
| **Qualité** | JUnit 5, Spring Security Test, Sonar (config projet), pipeline Jenkins (dossier CI) |

---

## Prérequis

- **Java 17** et **Maven 3.9+**
- **Node.js** (LTS recommandé) et **npm**
- **PostgreSQL** (instance accessible, schéma créé ou `ddl-auto` adapté)
- **Keycloak** démarré et realm / clients configurés pour l’API et les utilisateurs

---

## Démarrage rapide

### 1. Base de données

Créer une base PostgreSQL et un utilisateur, puis renseigner l’URL et les identifiants dans `backend/src/main/resources/application.properties` (voir [Configuration](#configuration)).

### 2. Keycloak

Des fichiers **Docker Compose** sont fournis sous `backend/docker/keycloak/` pour lancer un environnement type. Adapter les realms, clients et rôles (`ROLE_ADMIN`, `ROLE_AGENT`, `ROLE_PROPRIETAIRE`, `ROLE_CLIENT`) selon votre politique de sécurité.

### 3. API (backend)

```bash
cd backend
mvn spring-boot:run
```

L’API écoute par défaut sur **http://localhost:8080**.

### 4. Interface (frontend)

```bash
cd frontend
npm install
npm start
```

L’application Angular est servie en général sur **http://localhost:4200**.  
L’URL de l’API est définie dans `frontend/src/environments/environment.development.ts` (`apiBaseUrl`).

### 5. Build de production (frontend)

```bash
cd frontend
npm run build
```

---

## Configuration

Les paramètres sensibles **ne doivent pas** être commités en clair en production : préférer des variables d’environnement ou un fichier local non versionné.

| Fichier / zone | Rôle |
|----------------|------|
| `backend/.../application.properties` | Datasource PostgreSQL, issuer JWT Keycloak, client admin Keycloak, CORS, port serveur, uploads |
| `frontend/src/environments/environment*.ts` | URL de base de l’API (`apiBaseUrl`) |

**Exemples de clés backend** (noms indicatifs) :

- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- `keycloak.admin.*` (API admin pour création / sync utilisateurs)
- `app.cors.allowed-origins`

---

## Rôles et espaces applicatifs

| Rôle | Accès principal |
|------|-----------------|
| **Visiteur** | Accueil, annonces publiques, login, inscription (locataire / propriétaire), demande d’enregistrement d’agence |
| **ROLE_CLIENT** | Espace locataire : biens disponibles, demandes de location, suivi des demandes |
| **ROLE_PROPRIETAIRE** | Biens, mandats, contrats, tableau de bord patrimoine |
| **ROLE_AGENT** | Espace agence : mandats, biens liés, baux et demandes (validation, mise à jour) |
| **ROLE_ADMIN** | Vue d’ensemble, agences, agents, mandats, utilisateurs, comptes en attente d’activation, demandes de modification d’agence |

L’inscription publique crée souvent un compte **inactif** jusqu’à validation par un administrateur (Keycloak + base métier).

---

## API REST (aperçu)

Toutes les réponses métier suivent en général le format enveloppe `ApiRetour` (statut, message, données).

| Préfixe (exemples) | Usage |
|--------------------|--------|
| `/api/public/**` | Annonces, inscription |
| `/api/auth/**` | Authentification côté application si exposée |
| `/api/admin/**` | Administration (dont dashboard, utilisateurs Keycloak) |
| `/api/agent/**` | Espace agent (contrats, biens, mandats selon contrôleurs) |
| `/api/proprietaire/**` | Espace propriétaire |
| `/api/locataire/**` | Espace locataire |
| `/api/admin/dashboard/**` | Indicateurs (biens, revenus, retards, comptes en attente, etc.) |

Pour le détail des endpoints, se référer aux classes `@RestController` du package `com.example.gestionimmobilier.controller`.

---

## Qualité, tests et CI

```bash
# Backend — compilation et tests
cd backend
mvn verify

# Frontend — build
cd frontend
npm run build
```

- **SonarQube** : fichier `backend/sonar-project.properties` (adapter l’URL du serveur).
- **Jenkins** : `backend/Jenkinsfile` et compose sous `backend/docker/ci/` pour l’intégration continue.

---

## Documentation complémentaire

Une analyse plus détaillée (diagrammes UML, cas d’utilisation) peut être conservée dans un document séparé (`docs/`) pour ne pas alourdir ce fichier. Ce **README** se concentre sur la prise en main, la structure du projet et les bonnes pratiques d’exploitation.

---

## Licence et contribution

Projet académique / interne — préciser ici la licence (MIT, Apache 2.0, propriétaire, etc.) et les règles de contribution si le dépôt est partagé.

---

*Dernière mise à jour du README : mars 2026.*
